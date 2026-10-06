# Approved bounded retention

Miguel approved this policy on 2026-10-06. The retention layer installs a scheduler
and V11 login-history timestamp index. It deletes expired OAuth metadata immediately
on a subsequent pass and retains successful interactive-login history for 30 days.
The policy is enabled by default when this layer is deployed.

| Resource | Policy |
| --- | --- |
| Expired OAuth tokens | At most 2,000 per pass, oldest expiry first; cascading permission snapshots are deleted |
| Login history | At most 1,000 per pass, strictly older than 30 days; oldest first |
| Scheduling | Initial delay 10 seconds, then 10 seconds after each completed pass; dedicated single platform thread |
| Transaction | Both deletions commit atomically; explicit 3-second Spring transaction timeout |
| Database | Existing 2-second statement, 500ms lock and 3-second socket timeouts; DML-only runtime role |
| Concurrent maintenance | Local overlap guard; `FOR UPDATE SKIP LOCKED` skips rows held by another instance/transaction |

Live tokens, profiles, accounts, applications, credentials, grants and endpoint
identities are preserved. Failed passes roll back both deletions and retry after
the fixed delay. Logs report committed counts/duration or a failure category;
SQL parameters and exception messages are excluded. No unbounded backlog count
query runs as part of the scheduled worker.

## Deployment and disable procedure

1. Apply V11 with the migration role before enabling this layer. It creates
   `sessions_retention_time(session_time, session_id)`; runtime DML cannot create it.
   The normal transactional index build can block writes on a large existing
   history table; plan its migration window before production rollout.
2. Inspect eligible rows and take the appropriate backup before rollout. Both
   deletions are permanent; disabling the worker does not restore deleted data.
3. Set `RETENTION_ENABLED=false` (or `--app.retention.enabled=false`) and restart to
   disable the worker and its scheduler. Restart with `true` to resume. An in-flight
   transaction finishes or rolls back during bounded scheduler shutdown.
4. Watch committed batch counts/duration and rollback warnings. Repeated full
   batches or increasing eligible age signal backlog; do not raise fixed row limits
   without measuring the cascade cost and protecting foreground traffic.

Read-only operator queries (potentially expensive on a large backlog):

```sql
SELECT count(*) AS expired_tokens, min(expires_at) AS oldest_expiry
FROM auth_db.oauth_tokens WHERE expires_at <= CURRENT_TIMESTAMP;

SELECT count(*) AS old_login_records, min(session_time) AS oldest_login
FROM auth_db.sessions
WHERE session_time < CURRENT_TIMESTAMP - INTERVAL '30 days';
```

## Reproduce isolated validation

```sh
./gradlew release -Pskip-functional-tests --no-daemon --max-workers=1
python3 tools/validation/retention.py
```

The harness accepts no database target. It creates an owned disposable PostgreSQL
16 container with 1 CPU/512 MiB, no swap and a temporary data directory, applies
V1–V11 using the fixture role, proves runtime DDL denial and invokes the actual
production MyBatis mapper/transaction manager/scheduler with the DML-only role.
It removes the container and task-owned build processes even on failure.

Tests cover a maximum 2,000-token/1,000-login batch with 64 permissions per token,
live authorization-state preservation, exact expiry and 30-day boundaries, locked
rows and subsequent drain, a server-side timeout rolling back earlier token
removal, retry recovery, automatic scheduling/shutdown and disabled-policy
preservation beyond the initial delay. Unit tests verify concurrent invocation
cannot open another retention transaction. CI runs this harness after release.

On 2026-10-06 the six PostgreSQL checks passed locally. The maximum transaction
committed 2,000 tokens, 128,000 cascading permission rows and 1,000 old logins in
100–104ms across two passes on fresh limited databases; the next pass drained the remaining eligible rows.
The timeout fixture rolled back after approximately 2.1 seconds and the next pass
recovered. The serial release also passed 195 backend tests, required style,
website packaging and the existing 23 website tests/six snapshots. Generated legacy
TestNG functional tests remain compiled but skipped.

The nominal token ceiling is 200 tokens/s before transaction duration and missed
passes. A local maximum-batch timing is not sustained cleanup capacity, a production
measurement or evidence that mixed foreground traffic meets its latency target.
The existing capacity benchmark describes the earlier capacity-layer jar.
