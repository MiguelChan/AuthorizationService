CREATE TABLE auth_db.oauth_tokens (
    token_hash char(64) PRIMARY KEY,
    source_application_id integer NOT NULL REFERENCES auth_db.applications(application_id),
    target_application_id integer NOT NULL REFERENCES auth_db.applications(application_id),
    source_credential_version bigint NOT NULL CHECK (source_credential_version > 0),
    target_credential_version bigint NOT NULL CHECK (target_credential_version > 0),
    issuer varchar(512) NOT NULL,
    issued_at timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL CHECK (expires_at > issued_at),
    revoked_at timestamp with time zone
);
CREATE INDEX oauth_tokens_expiry ON auth_db.oauth_tokens(expires_at);

CREATE TABLE auth_db.oauth_token_permissions (
    token_hash char(64) NOT NULL REFERENCES auth_db.oauth_tokens(token_hash) ON DELETE CASCADE,
    grant_id integer NOT NULL REFERENCES auth_db.application_grants(grant_id),
    grant_version bigint NOT NULL CHECK (grant_version > 0),
    PRIMARY KEY (token_hash, grant_id)
);
