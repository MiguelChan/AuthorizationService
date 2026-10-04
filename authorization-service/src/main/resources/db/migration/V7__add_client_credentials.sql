CREATE TABLE auth_db.client_credentials (
    application_id integer PRIMARY KEY REFERENCES auth_db.applications(application_id),
    client_id varchar(36) NOT NULL UNIQUE,
    secret_hash varchar(255) NOT NULL,
    version bigint NOT NULL DEFAULT 1 CHECK (version > 0),
    issued_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked_at timestamp with time zone
);
