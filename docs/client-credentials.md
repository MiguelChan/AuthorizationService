# Application client credentials

Application creation returns `applicationId`, `clientId` (random UUID) and `clientSecret`
(32 cryptographically random bytes, encoded as 43 base64url characters). Save the secret
from this response: it cannot be retrieved later. Responses have `Cache-Control: no-store`.
Only a peppered BCrypt hash is stored using the existing password-hashing configuration.
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
