# Service-to-service OAuth client credentials

This initial flow is for confidential server applications calling a registered
endpoint in another application. It does not support user delegation, browser
public clients, password grants, authorization codes, refresh tokens or scheduling.
Use owner-generated client credentials from #20. Never embed these secrets in a SPA.

## Protocol

POST `/oauth/token` as `application/x-www-form-urlencoded`, authenticating with
`Authorization: Basic base64(clientId:clientSecret)` using RFC 6749's form encoding
for each credential component. Only `client_credentials` is accepted. Required
`audience` is the recipient's public client ID (an explicit extension); optional
`scope` is a space-separated set of its registered action names. Omitted scope
selects currently granted actions, bounded to 64. Unknown/disallowed scopes and an
empty permission set fail with `invalid_scope`; unsupported grants fail with
`unsupported_grant_type`. Invalid client credentials fail with HTTP 401,
`invalid_client` and a Basic challenge. Parameters belong in the form body; query
parameters, duplicates and body client passwords are rejected. The contract accepts
Basic authentication only, avoiding multiple client authentication mechanisms.

Success contains `access_token`, `token_type: Bearer`, `expires_in` and `scope`.
A token is 256 random bits (43 base64url characters); only its SHA-256 digest is
stored. Default expiry is five minutes, configurable from one second to one hour.
Token and permission snapshots are committed together. Each token binds to one
source, one recipient audience, the issuer, both credential versions and explicit
grant versions. The issuer is configured with `OAUTH_ISSUER` outside dev. No JWT
is used: an online database check allows revocation and deactivation to take effect
on every validation without a stale self-contained claim or cache.

POST `/oauth/introspect` with the recipient's Basic client credentials and the
form `token`. Invalid, expired, revoked, wrong-audience and no-longer-authorized
tokens return exactly `{"active":false}`. Active responses include `client_id`,
`sub`, `iss`, `aud`, `iat`, `exp`, the remaining live `scope`, and the `permissions`
extension listing endpoint IDs/actions/methods/path templates. Only the authenticated
recipient can introspect its tokens. Source/target app or credential deactivation,
credential rotation, endpoint deactivation and grant revocation/version changes
are evaluated online. Removing one grant removes that permission; other valid
permissions remain usable. Regranting never restores a previous token's permission.

POST `/oauth/revoke` with source Basic credentials and `token` returns HTTP 200 for
revoked, unknown or foreign tokens; only a source-owned token is changed. Responses
and errors use no-store/no-cache headers and never echo secrets. OAuth has a separate
stateless security chain and cannot use user sessions or impersonate users on `/api`.

## Receiving application enforcement

The receiving service maps each actual HTTP route to its registered endpoint ID
and required action. Before executing the resource it sends the Bearer token to
introspection using its own client secret, validates issuer/audience/expiry, and
requires the exact endpoint ID, HTTP method and action in `permissions` and `scope`.
Missing/invalid tokens fail with 401; missing permissions fail with 403. Fail closed
if introspection is unavailable. Do not cache introspection for this initial flow.
The Python example in `examples/python/opaque_token_resource.py` implements that
boundary and is used by the isolated integration tests. Catalog registration never
proxies a resource or performs a remote endpoint call.

## Deployment and lifecycle

OAuth endpoints require HTTPS. Dev explicitly permits plain HTTP only when both
the connection peer and local address are loopback; this switch defaults to false
outside dev. Configure trusted TLS termination and secure request forwarding in
production rather than trusting arbitrary forwarded headers. Configure a stable
HTTPS `OAUTH_ISSUER` and `app.oauth.token-ttl-seconds`; changing the issuer invalidates
older tokens. Use HTTPS for owner secret delivery as well. Migration V10 adds opaque
tokens and their permission snapshots; it grants no access to existing applications.
Expired token rows can be pruned with `DELETE FROM auth_db.oauth_tokens WHERE
expires_at < CURRENT_TIMESTAMP`; permission snapshots cascade on deletion.

## References and decisions

- [RFC 6749](https://www.rfc-editor.org/rfc/rfc6749): client credentials, client authentication, token responses and errors.
- [RFC 6750](https://www.rfc-editor.org/rfc/rfc6750): receiving-service Bearer handling.
- [RFC 7662](https://www.rfc-editor.org/rfc/rfc7662): authenticated introspection and inactive responses.
- [RFC 7009](https://www.rfc-editor.org/rfc/rfc7009): source token revocation.
- [RFC 9700](https://datatracker.ietf.org/doc/html/rfc9700): current security guidance and restricted token privileges.

These contracts and the opaque-token decision implement #40 under Miguel's request
to complete the remaining issues. The service relies on online validation and both
receiving application owners' explicit directional grants, without adding scheduling or delegated
user flows. This is the initial client-credentials subset, not every OAuth grant type.
