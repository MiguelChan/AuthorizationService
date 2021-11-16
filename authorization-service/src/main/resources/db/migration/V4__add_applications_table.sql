create table auth_db.applications (
    application_id serial primary key,
    profile_id varchar not null,
    app_name varchar not null,
    app_icon varchar default null,
    app_home_page varchar default null,
    short_description varchar not null,
    redirect_url varchar not null,
    constraint fk_profile foreign key (profile_id) references auth_db.profiles(profile_id)
);

-- Uncomment these when developing in your local computer
--grant usage on schema auth_db to app_user;
--grant all privileges on all tables in schema auth_db to app_user;
--alter default privileges in schema auth_db grant all on tables to app_user;
--GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA auth_db TO app_user;
