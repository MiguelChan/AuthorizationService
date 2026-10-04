# Directed application grants

A grant authorizes `source application -> target application -> endpoint/action`.
The target application's authenticated user owner controls POST/GET
`/api/applications/{targetId}/grants` and DELETE `/{grantId}`. Owning the source
application does not let a caller grant themselves access to someone else's
resources. POST body: `{"sourceApplicationId":1,"endpointId":2}`. The server
assigns target, ID, version and active state; the target is always the URL owner.
Both applications and the target endpoint must be active. Catalog endpoint identity
is immutable. A foreign key prevents a target from referencing another app's endpoint.

No grant means deny, including reverse direction and self-access. Bidirectional
access requires two explicit directional grants approved by the respective target
owners. Already-active duplicates return 409. Revocation returns 204, marks the grant
inactive and increases its version; missing/already-revoked grants return 404.
Reissuing a revoked grant increases its version again, so previously issued tokens
cannot regain access. Lists retain revoked entries. Permission evaluation requires
active source, target, endpoint and grant. Deactivating any of these denies access.

Migration V9 creates an empty permission table; it grants no implicit access.
OAuth tokens bind to these grant versions and the target audience. The receiving
application must introspect and enforce the endpoint/action before executing a
resource; the authorization service does not proxy registered endpoint calls.
These decisions implement #39 under Miguel's request to complete the remaining
issues. Scheduling, delegated user access and wildcard permissions are excluded.
