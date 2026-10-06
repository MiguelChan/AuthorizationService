# Bounded capacity and local verification

The declared initial workload is authenticated opaque-token introspection with an
active permission: receiving-client Basic authentication, issuer/audience binding
and online application/credential/grant/endpoint checks. It is not a ping benchmark
and does not claim 1,000 BCrypt user logins or token mints per second. Human
passwords remain BCrypt cost 11 in validation. Generated 256-bit client secrets use
a versioned HMAC-SHA256 fingerprint; compatible BCrypt credentials upgrade after
a guarded, compare-and-set verification without version rotation.

## Enforced bounds

| Resource | Default bound and behavior |
| --- | --- |
| Dynamic requests in flight | 1,024; no application wait queue; excess receives 503/Retry-After |
| Request body | 16 KiB, including unknown-length/chunked uploads; excess receives 413 |
| Body time | Two-second elapsed budget checked around each blocking read; configured socket read adds at most two seconds; timeout returns 408/close; rejected bodies are not drained |
| Headers/query | 16 KiB Tomcat headers, 1,024-character Authorization, 2,048-character query |
| Body form parameters | At most 64 keys and 64 duplicate values per key; duplicate OAuth parameters rejected |
| Global/peer/client rate | 5,000/s / 3,000/s / 2,000/s, token-bucket bursts equal to those rates |
| Token minting | 100/s globally, burst 200; 20/s per public client ID, burst 40 |
| Signup/login peer rate | 2/s burst 10 / 10/s burst 20 |
| Expensive authentication work | Eight login checks, eight registration requests, eight legacy-client checks; independent finite budgets |
| Login failures | Five failures per normalized identity block further checks for 60 seconds |
| Rate/failure state | 10,000 keys each; fail closed at capacity; no active-key eviction that resets limits |
| Database pool | Fixed 32 connections; acquire timeout 1s, validation 500ms, socket 3s |
| Database operations | Statement 2s, lock 500ms, transaction default 3s |
| Tomcat | 2,048 connections, accept backlog 64, connection timeout 2s, keepalive 15s |
| Catalog pages | Keyset cursor, default/maximum 100 entries; endpoint identity quota 1,000 per app including inactive |
| Token authorization state | At most 64 scopes; live introspection reads at most 65 rows and rejects oversized legacy state |

All rate budgets are **per instance**, not a distributed quota. Peer limits use the
resolved socket peer; arbitrary X-Forwarded-For/Proto headers are not trusted by
the admission filter. Behind a gateway, configure and test trusted forwarding
explicitly and enforce a distributed quota at that boundary. Unauthenticated
traffic can consume admission/public-client budgets; these bounds control resource
use and do not promise immunity to distributed denial of service.

Production requires HTTPS; dev must be explicitly selected for local HTTP. A
verified direct TLS server is used below. Provide external secrets, exact CORS
origins, migration/runtime database roles and deployment-specific trusted ingress.
Do not grant the application DDL rights. The subsequent approved
[retention layer](retention-proposal.md) prunes expired tokens and login history
older than 30 days in fixed batches and provides an explicit disable switch.

## Reproduce safely

Prerequisites: Java 25 JDK, Go, Python 3 and Docker. Build serially first:

```sh
./gradlew release -Pskip-functional-tests --no-daemon --max-workers=1
python3 tools/validation/run.py
python3 tools/validation/run.py --host-app --app-cpus 4
python3 tools/validation/run.py --host-app --app-cpus 8
```

The first command runs the existing backend/frontend release checks. Generated
legacy TestNG tests are compiled but skipped; this is not an executed E2E claim.
The Python harness separately tests the packaged jar and an actual receiving
process with disposable PostgreSQL, applying all committed migrations in numeric order (V1–V11 with retention).

Default restricted stack: app 2 CPU/768 MiB, database 1 CPU/512 MiB, swap disabled, PID limits 128, non-root/read-only application, dropped capabilities,
no-new-privileges and DML-only runtime role. The Docker VM used here has **two CPU
total**, shared by both containers; per-container caps are maxima, not reservations.
No Docker settings are changed. CPU requests exceeding its capacity fail preflight.

The host comparison uses an explicit bounded heap and four/eight JVM-visible processors.
It has **no OS CPU/RAM quota** and is not equivalent to a four/eight-CPU container. Both
profiles retain the limited PostgreSQL container. The JVM recipe uses G1,
InitialRAMPercentage=MaxRAMPercentage=60, MaxGCPauseMillis=50 and an explicit
ActiveProcessorCount. Apply this recipe to deployment configuration; application
properties alone do not select a GC. The small default JVM initially selected
Serial GC and a 12 MiB heap and showed avoidable tail pressure.

The harness offers 100/s for 5s, 500/s for 10s, then 1,000/s for 60s (tunable
1..300s), and separately releases 1,000 simultaneous requests after a concurrent
connection warmup. Each phase has 100 sequential authorization warmup requests.
TLS certificates are hostname-verified with a disposable CA; TLS validation is
never disabled. The receiving process proves bearer -> introspection -> exact
route permission, before/after revocation.

Every response must be authorized for the exact recipient. Measurements include
scheduled/completed/authorized work, actual status codes, transport failures,
drops, max in-flight, and p50/p95/p99 **from scheduled arrival**, including client
queue delay. The load client bounds jobs and concurrency; rejects are not successful
throughput. Provisional acceptance is p95 <=250ms, <1% rejected/unexpected demand
and at least 99% of offered rate (allowing failures/drain-time at a finite boundary).
The harness exits nonzero when any phase fails; cold-start and burst failures stay
visible even when the sustained target passes.

Evidence is printed to a private temporary directory. Secret fixtures/private
keys are removed; processes, containers and network are cleaned on success, error
or interruption. Read sanitized measurement JSON/resource manifests; logs may
contain disposable identifiers and are not committed. `--diagnose-pool` deliberately
enables extra logging and can alter latency; final measurements leave it off.

## Measured results and practical limits

The sanitized [measurement record](benchmarks/2026-10-06.json) records the capacity-layer jar before retention,
resources, all phases and errors. Results are local observations, not production
capacity or a long-duration reliability guarantee.

| Profile / phase | Authorized / offered | Authorized rps | p95 / p99 ms | Rejected demand | Max in flight |
| --- | --- | --- | --- | --- | --- |
| host-4: ramp-100 | 500 / 500 | 100.075 | 5.601 / 6.238 | 0.0000% | 2 |
| host-4: ramp-500 | 5,000 / 5,000 | 500.047 | 291.521 / 487.705 | 0.0000% | 193 |
| host-4: target-1000 | 60,000 / 60,000 | 1000.000 | 1.055 / 4.626 | 0.0000% | 85 |
| host-4: burst-1000 | 1,000 / 1,000 | 3860.015 | 254.688 / 258.470 | 0.0000% | 1000 |
| restricted-2: ramp-100 | 500 / 500 | 100.153 | 4.900 / 8.901 | 0.0000% | 2 |
| restricted-2: ramp-500 | 4,814 / 5,000 | 481.447 | 1001.176 / 1139.119 | 3.7200% | 473 |
| restricted-2: target-1000 | 59,937 / 60,000 | 998.955 | 1.697 / 589.428 | 0.1050% | 1000 |
| restricted-2: burst-1000 | 1,000 / 1,000 | 3427.020 | 281.975 / 290.178 | 0.0000% | 1000 |
| host-8: ramp-100 | 500 / 500 | 100.147 | 5.523 / 6.548 | 0.0000% | 2 |
| host-8: ramp-500 | 5,000 / 5,000 | 500.038 | 255.157 / 412.265 | 0.0000% | 153 |
| host-8: target-1000 | 60,000 / 60,000 | 1000.000 | 1.057 / 3.702 | 0.0000% | 110 |
| host-8: burst-1000 | 1,000 / 1,000 | 3643.261 | 235.737 / 273.725 | 0.0000% | 1000 |

The earlier two-CPU container run completed 60,000/60,000 at 1,000.004 authorized
requests/s (p95 1.114ms, p99 51.543ms), and a 1,000-in-flight burst with p95 231.112ms.
Its cold 500/s ramp rejected 181/5,000 requests and had p95 946.807ms. The subsequent
final-jar run and host comparison are recorded above rather than selecting only
the best result. Tail variability, startup/JIT/pool pressure and limited CPU headroom
remain relevant. Do not present a clean sustained phase as proof of clean startup.

Introspection originally performed four SQL calls per request. It now performs
client authentication plus one SQL snapshot that checks all authorization state.
No positive permission cache survives revocation. Basic reads no longer add one SQL
login-history row per request; actual browser login adds one. Temporary pool/SQL
failures return 503/Retry-After with rate-bounded logs of category/SQLState only,
never credentials, SQL parameters or exception messages.

Before a production claim, validate cold-start readiness/warmup, prolonged mixed
traffic, larger/varied tenants and 64-scope tokens, realistic network latency,
receiving-service concurrency, ingress absolute header/TLS deadlines, database/network TLS, distributed rates and
monitoring. The current fixture reuses one active token/permission and a 1,000-entry
owner catalog. Token issuance has its own protective budget and was not benchmarked
at 1,000/s. Frontend dependency debt and consumer membership/user roles remain
explicit in [the design/security audit](security-audit-2026-10-05.md).
