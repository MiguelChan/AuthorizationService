ALTER TABLE auth_db.applications
ADD COLUMN app_type varchar NOT NULL DEFAULT 'SERVICE';

ALTER TABLE auth_db.applications
ADD CONSTRAINT applications_app_type_check CHECK (app_type IN ('SERVICE', 'WEB_SERVICE'));
