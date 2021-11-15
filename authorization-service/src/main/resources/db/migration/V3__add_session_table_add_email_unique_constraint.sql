create table auth_db.sessions (
    session_id serial primary key,
    account_id varchar not null,
    session_type varchar not null,
    session_time timestamp with time zone not null,
    constraint fk_account foreign key (account_id) references auth_db.accounts(account_id) on delete cascade
);

alter table auth_db.profiles
add constraint prof_unique_email UNIQUE (email_address);

alter table auth_db.accounts
add constraint acc_unique_email UNIQUE (email);

-- Uncomment these when developing in your local computer
-- grant usage on schema auth_db to app_user;
-- grant all privileges on all tables in schema auth_db to app_user;
-- alter default privileges in schema auth_db grant all on tables to app_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA auth_db TO app_user;
