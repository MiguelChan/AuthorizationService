create table auth_db.applications (
    application_id serial primary key,
    profile_id varchar not null,
    app_name varchar not null,
    app_icon varchar default null,
    app_home_page varchar default null,
    short_description varchar not null,
    redirect_url varchar not null,
    is_active boolean not null,
    constraint fk_profile foreign key (profile_id) references auth_db.profiles(profile_id)
);