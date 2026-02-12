create table friendship (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    friend_id uuid not null references app_user(id),
    created_at timestamp not null,
    constraint chk_friendship_not_self check (user_id <> friend_id),
    unique (user_id, friend_id)
);

create table friend_invite (
    id uuid primary key,
    code varchar(64) not null unique,
    inviter_user_id uuid not null references app_user(id),
    created_at timestamp not null,
    expires_at timestamp not null,
    used_at timestamp
);

create index idx_friendship_user_id on friendship(user_id);
create index idx_friendship_friend_id on friendship(friend_id);
create index idx_friend_invite_code on friend_invite(code);
