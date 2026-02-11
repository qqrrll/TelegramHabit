create table app_user (
    id uuid primary key,
    telegram_id bigint not null unique,
    username varchar(255),
    first_name varchar(255),
    last_name varchar(255),
    photo_url text,
    created_at timestamp not null
);

create table habit (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    title varchar(255) not null,
    type varchar(20) not null,
    times_per_week integer,
    color varchar(32) not null,
    icon varchar(32) not null,
    is_archived boolean not null default false,
    created_at timestamp not null,
    constraint chk_habit_type check (type in ('DAILY', 'WEEKLY')),
    constraint chk_habit_times check (
        (type = 'DAILY' and times_per_week is null) or
        (type = 'WEEKLY' and times_per_week is not null and times_per_week between 1 and 7)
    )
);

create table habit_completion (
    id uuid primary key,
    habit_id uuid not null references habit(id),
    date date not null,
    completed boolean not null,
    created_at timestamp not null,
    unique (habit_id, date)
);

create table activity_log (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    habit_id uuid references habit(id),
    type varchar(20) not null,
    message text not null,
    created_at timestamp not null,
    constraint chk_activity_type check (type in ('COMPLETED', 'STREAK', 'RECORD'))
);

create index idx_habit_user_id on habit(user_id);
create index idx_completion_habit_date on habit_completion(habit_id, date);
create index idx_activity_user_created_at on activity_log(user_id, created_at desc);
