-- The commands below need to be run with the admin User.
create user flyway_user with password 'flyway-user-password';
create user app_user with password 'app-user-password';

create schema auth_db;

grant all privileges on schema auth_db to flyway_user;
grant usage on schema auth_db to app_user;
grant all privileges on all tables in schema auth_db to app_user;
alter default privileges in schema auth_db grant all on tables to app_user;