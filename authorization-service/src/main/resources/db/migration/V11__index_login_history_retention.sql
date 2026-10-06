-- Applied by the migration role; runtime retention requires DML privileges only.
CREATE INDEX sessions_retention_time ON auth_db.sessions(session_time, session_id);
