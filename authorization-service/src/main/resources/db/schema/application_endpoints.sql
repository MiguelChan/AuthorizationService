CREATE TABLE auth_db.application_endpoints (
    endpoint_id serial PRIMARY KEY,
    application_id integer NOT NULL REFERENCES auth_db.applications(application_id),
    http_method varchar(7) NOT NULL CHECK (http_method IN ('GET','POST','PUT','PATCH','DELETE','HEAD','OPTIONS')),
    path varchar(512) NOT NULL,
    action varchar(64) NOT NULL,
    description varchar(1024) NOT NULL DEFAULT '',
    is_active boolean NOT NULL DEFAULT true,
    UNIQUE (application_id, http_method, path),
    UNIQUE (application_id, action),
    UNIQUE (application_id, endpoint_id)
);
