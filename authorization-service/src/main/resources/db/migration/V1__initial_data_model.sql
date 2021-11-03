create table auth_db.profiles (
    profile_id varchar not null primary key,
    first_name varchar not null,
    last_name varchar not null,
    email_address varchar not null,
    phone_number varchar(10) not null
);

create table auth_db.accounts (
    account_id varchar not null primary key,
    profile_id varchar not null,
    account_type varchar not null,
    constraint fk_profile foreign key (profile_id) references auth_db.profiles(profile_id) on delete cascade
);

create table auth_db.entities (
    entity_id varchar not null primary key,
    profile_id varchar not null,
    constraint fk_profile foreign key (profile_id) references auth_db.profiles(profile_id) on delete cascade
);