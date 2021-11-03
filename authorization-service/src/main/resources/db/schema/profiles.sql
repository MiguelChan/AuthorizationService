create table auth_db.profiles (
    profile_id varchar not null primary key,
    first_name varchar not null,
    last_name varchar not null,
    email_address varchar not null,
    phone_number varchar(10) not null
);