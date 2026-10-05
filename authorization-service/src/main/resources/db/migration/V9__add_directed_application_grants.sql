CREATE TABLE auth_db.application_grants (
    grant_id serial PRIMARY KEY,
    source_application_id integer NOT NULL REFERENCES auth_db.applications(application_id),
    target_application_id integer NOT NULL REFERENCES auth_db.applications(application_id),
    endpoint_id integer NOT NULL,
    version bigint NOT NULL DEFAULT 1 CHECK (version > 0),
    is_active boolean NOT NULL DEFAULT true,
    FOREIGN KEY (target_application_id, endpoint_id) REFERENCES auth_db.application_endpoints(application_id, endpoint_id),
    UNIQUE (source_application_id, endpoint_id)
);
