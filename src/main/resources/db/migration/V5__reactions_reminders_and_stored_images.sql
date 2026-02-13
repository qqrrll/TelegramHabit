create table stored_image (
    id uuid primary key,
    content_type varchar(128) not null,
    data bytea not null,
    created_at timestamp not null
);

create table habit_reaction (
    id uuid primary key,
    habit_id uuid not null references habit(id),
    reactor_user_id uuid not null references app_user(id),
    emoji varchar(16) not null,
    created_at timestamp not null,
    unique (habit_id, reactor_user_id, emoji)
);

create index idx_habit_reaction_habit on habit_reaction(habit_id);

create table reminder_log (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    date date not null,
    sent_at timestamp not null,
    unique (user_id, date)
);

create index idx_reminder_log_user_date on reminder_log(user_id, date);
