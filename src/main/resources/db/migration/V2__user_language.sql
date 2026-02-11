alter table app_user
    add column if not exists language varchar(8) not null default 'en';
