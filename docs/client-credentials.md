# Application client credentials

Application creation returns `applicationId`, `clientId` (random UUID) and `clientSecret`
(32 cryptographically random bytes, encoded as 43 base64url characters). Save the secret
from this response: it cannot be retrieved later. Responses have `Cache-Control: no-store`.
Only a versioned `hmac-sha256$` fingerprint keyed by the external pepper is stored.
These are server-generated 256-bit secrets; human passwords retain peppered BCrypt.
Existing BCrypt client hashes remain compatible and upgrade after a successful check
using a version/hash compare-and-set, without rotating the secret or invalidating tokens.
At most eight legacy client verifications run at once; excess demand receives 503.
A concurrent credential rotation or revocation cannot be overwritten by this upgrade.
Credential entity/response string representations omit the secret and its hash.

An authenticated application owner can POST `/api/applications/{id}/credentials/rotate`
to issue a replacement, including for migrated applications that have no credentials.
The client ID stays stable, the credential version increases and the old secret stops
working. DELETE `/api/applications/{id}/credentials` revokes the current credential.
Applications must be active; nonowners receive 403 and missing applications receive 404.
There is no endpoint for retrieving a stored secret. Deactivating an application also
prevents client authentication. Existing applications are not given undisclosed secrets
by migration V7. Creation and initial credential issuance are one transaction; concurrent
rotations serialize on the application row. OAuth token records will bind to credential
versions so rotation/revocation can invalidate previously issued tokens.

Use HTTPS for credential delivery and token exchange in deployed environments.
