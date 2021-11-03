create table auth_db.entities (
    entity_id varchar not null primary key,
    profile_id varchar not null,
    constraint fk_profile foreign key (profile_id) references auth_db.profiles(profile_id) on delete cascade
);