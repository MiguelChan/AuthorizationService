# AuthorizationService design and security audit — 2026-10-05

## Intended behavior versus current capability

The [phase-0 design](../design-docs/phase0/DesignDocument.md) describes a multi-tenant authentication, authorization and identity hub. Application administrators onboard applications; consumer identities belong to an application and carry read/create/update/delete/admin permissions. It proposes user bearer/refresh tokens, user CRUD with soft deletion, failed-login blocking and accountable operations.

| Capability | Current implementation | Remaining work |
| --- | --- | --- |
| Administrator registration and login | Global profile/account, Basic and browser session | Email verification, account recovery/MFA, explicit account deactivation and session revocation |
| Application ownership | Owner derived from authenticated profile; cross-owner mutation rejected | Application listing and administration UI |
| Confidential service authentication | One-time credentials, rotation/revocation, versioned opaque tokens | Operational rotation guidance, bounded token retention and monitoring |
| Service permission enforcement | Target-owned directional endpoint/action grants; recipient introspection | Production receiving-service integration and deployment validation |
| Application consumer identities | Existing entities are global administrator identities | Explicit application membership model; cross-tenant tests |
| Consumer-user roles | Authentication tokens have no granted authorities; owner checks are the policy | Application-scoped read/create/update/delete/admin policy and owner-controlled assignment; never accept caller-assigned roles |
| User token/refresh flow | No consumer-user token or refresh implementation | Separate approved OAuth/OIDC flow; do not add a password grant or reuse confidential service tokens as user identities |
| Auditability | Legacy session rows are written on every Basic request | Separate successful login/authorization/security-event records with bounded retention and redaction |

The original JWT suggestion differs from the explicitly documented opaque service-token decision in [the current contract](oauth-client-credentials.md). Current online authorization supports immediate credential/grant revocation. This audit does not reintroduce self-contained permission caching or imply complete OAuth/OIDC support.

## Reproduced local findings

These results use disposable users and isolated PostgreSQL 16 against mainline `400091e`; no production system was probed.

| Finding | Evidence before correction | Impact and correction |
| --- | --- | --- |
| Missing browser CSRF enforcement | Cross-origin form login succeeds; cookie-authenticated PUT /api/profile without a CSRF token returns 200 and changes SQL data | Login/session forgery; require browser CSRF for login and mutations, expose an uncached token endpoint and update the SPA |
| Arbitrary credentialed CORS | Profile response reflects http://localhost:18096 and sends Access-Control-Allow-Credentials: true | Untrusted origins can read credentialed responses when cookies are available, including a same-site different-origin attacker; use exact configured origins |
| Unbounded registration fields | A 65,536-character first name returns 200 and persists | Memory/storage abuse; bound names/email/password before regex/hash/database work |
| Basic authentication creates a SQL session per request | Ten profile reads insert ten session rows | Write amplification and unbounded history; separate session creation from stateless reads |
| Expensive repeated credential verification | Ten Basic profile reads average 112.91ms with BCrypt cost 11 on the local host | Validation throughput cannot be inferred from cheap ping benchmarks; bound admission and measure the real authentication/introspection path |
| Development profile selected by default | Main properties activate dev, including a repository pepper and loopback HTTP exception | Accidental insecure deployment; production is the default and dev must be explicitly selected |

Cookie mutation and CORS findings are reproduced server behavior. SameSite cookie defaults and browser-origin deployment affect exploit delivery; they do not replace CSRF or an origin allowlist. Baseline raw fixtures/results and process cleanup are recorded under `/private/tmp/authorization-modernization-20261005`; secrets do not belong in this document.

## Security test boundaries

The regression suite covers CSRF before execution, session-plus-Basic attempts to bypass it, credentialed CORS rejection, owner isolation, immutable endpoint identities, directed/reverse permissions, recipient binding, token expiry/revocation, credential rotation and parameter validation. HTTP tests and real receiving-service tests are required in addition to mocked unit tests. Attack probes are bounded and restricted to the local disposable environment.

Further review includes authentication brute force, header/query/body bounds, SQL-injection resistance, malformed/duplicate credentials, path normalization, grant-version changes, oversized/chunked requests, pagination and overload behavior. A passing suite is evidence for its tested cases, not a claim that every possible vulnerability has been eliminated.

## Capacity acceptance

Initial target: 1,000 completed requests/s and a separate burst test with up to 1,000 in-flight requests. Provisional acceptance is p95 <=250ms and <1% unexpected errors for the declared sustained workload. Report offered and completed rates, status codes, dropped work, p50/p95/p99, warmup, duration, BCrypt cost, resources and database configuration. Rate-limited responses count as rejected demand rather than successful throughput.

Measure authenticated OAuth introspection with active permissions; liveness traffic alone cannot establish the target. Run a resource-constrained container profile as well as the host profile. Keep request bodies, headers, queues, concurrent work, connection pools, credential-verification state and catalog pages bounded. Never cache authorization outcomes across revocation.
