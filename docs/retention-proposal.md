# Retention policy proposed for review

Status: decision required. No automatic deletion job or new retention migration is
installed. The capacity stack does not delete real tokens or login history.

The request and issuance limits bound instantaneous work, but stored expired tokens
and login history still accumulate. Proposed policy:

- Delete expired OAuth token metadata and its cascading permission snapshots after
  expiry. Bound each pass to 2,000 token rows, use `FOR UPDATE SKIP LOCKED`, run no
  overlapping batches, and apply a three-second transaction deadline.
- Retain successful interactive-login records for 30 days, then delete at most 1,000
  rows per pass. This permanently removes old account/time audit evidence.
- Start at a ten-second fixed delay and measure backlog/drain rate. Live tokens,
  credential versions, grants, accounts and profiles are excluded.
- Add a timestamp index on login history before rollout. The token expiry index
  already exists. Separate migration privileges from runtime DML privileges.

Read-only review queries:

```sql
SELECT count(*) AS expired_tokens FROM auth_db.oauth_tokens
WHERE expires_at <= CURRENT_TIMESTAMP;

SELECT count(*) AS old_login_records FROM auth_db.sessions
WHERE session_time < CURRENT_TIMESTAMP - INTERVAL '30 days';
```

Before enabling, decide whether expired token metadata also requires an audit
retention window, approve the login-history period, test cascading deletion with
64 permissions per token, verify live-state preservation and cleanup throughput,
and define an operator-controlled rollback/disable procedure. The proposed rate
would exceed the single-instance 100-token/s mint budget when batches complete
within the deadline; this is a sizing estimate, not measured deletion capacity.

Automatic approval review rejected enabling this destructive policy because Miguel
had not approved the specific 30-day history retention or permanent deletion. The
reviewable proposal is retained here; approval is needed before implementation.
