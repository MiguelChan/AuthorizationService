create table auth_db.sessions (
    session_id serial primary key,
    account_id varchar not null,
    session_type varchar not null,
    session_time timestamp with time zone not null,
    constraint fk_account foreign key (account_id) references auth_db.accounts(account_id) on delete cascade
);