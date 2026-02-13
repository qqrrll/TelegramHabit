create table activity_reaction (
    id uuid primary key,
    activity_id uuid not null references activity_log(id) on delete cascade,
    reactor_user_id uuid not null references app_user(id),
    emoji varchar(16) not null,
    created_at timestamp not null,
    unique (activity_id, reactor_user_id, emoji)
);

create index idx_activity_reaction_activity_id on activity_reaction(activity_id);

create table app_notification (
    id uuid primary key,
    recipient_user_id uuid not null references app_user(id),
    actor_user_id uuid not null references app_user(id),
    activity_id uuid references activity_log(id) on delete set null,
    type varchar(32) not null,
    message text not null,
    is_read boolean not null default false,
    created_at timestamp not null
);

create index idx_notification_recipient_created_at on app_notification(recipient_user_id, created_at desc);
create index idx_notification_recipient_read on app_notification(recipient_user_id, is_read);
