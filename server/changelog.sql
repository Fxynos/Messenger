-- liquibase formatted sql
-- changeset Fxynos:1
create table user(
    id int primary key auto_increment,
    login varchar(20) not null,
    password varbinary(256) not null,
    email varchar(256),
    image varchar(256),
    hidden bool not null default false,
    index (login),
    check (email regexp "^[a-zA-Z_0-9.-]+@([a-zA-Z_0-9-]+.)+[a-zA-Z_0-9-]{2,4}$")
);
create table phone(
    user_id int primary key,
    country int not null,
    number bigint not null,
    foreign key (user_id) references user(id) on delete cascade,
    index (country, number),
    check (country > 0),
    check (number > 0)
);
-- rollback drop table phone;
-- rollback drop table user;
-- changeset Fxynos:2
create table black_list(
    user_id int,
    blocked_id int,
    primary key (user_id, blocked_id),
    foreign key (user_id) references user(id) on delete cascade,
    foreign key (blocked_id) references user(id) on delete cascade
);
create table friend(
    user_id int,
    friend_id int,
    primary key (user_id, friend_id),
    foreign key (user_id) references user(id) on delete cascade,
    foreign key (friend_id) references user(id) on delete cascade
);
-- rollback drop table black_list;
-- rollback drop table friend;
-- changeset Fxynos:3
create table conversation(
    id bigint primary key auto_increment,
    name varchar(20) not null,
    image varchar(256)
);
create table conversation_rights(
    id int primary key auto_increment,
    role varchar(20) not null,
    edit_members bool not null default false,
    edit_data bool not null default false,
    edit_rights bool not null default false,
    get_reports bool not null default false
);
create table participate(
    user_id int,
    conversation_id bigint,
    rights_id int not null,
    primary key (user_id, conversation_id),
    foreign key (user_id) references user(id) on delete cascade,
    foreign key (conversation_id) references conversation(id),
    foreign key (rights_id) references conversation_rights(id)
);
-- rollback drop table participate;
-- rollback drop table conversation;
-- rollback drop table conversation_rights;
-- changeset Fxynos:4
create table message(
    id bigint primary key auto_increment,
    sender_id int not null,
    time timestamp not null,
    content varchar(1000) not null,
    foreign key (sender_id) references user(id)
);
create table private_message(
    message_id bigint primary key,
    receiver_id int not null,
    foreign key (message_id) references message(id) on delete cascade,
    foreign key (receiver_id) references user(id)
);
create table conversation_message(
    message_id bigint primary key,
    conversation_id bigint not null,
    foreign key (message_id) references message(id) on delete cascade,
    foreign key (conversation_id) references conversation(id) on delete cascade
);
-- rollback drop table private_message;
-- rollback drop table conversation_message;
-- rollback drop table message;
-- changeset Fxynos:5
create table notification(
    id bigint primary key auto_increment,
    user_id int not null,
    time timestamp not null,
    title varchar(40),
    content varchar(1000),
    seen bool not null default false,
    foreign key (user_id) references user(id) on delete cascade,
    check (content not regexp "^$")
);
create table friend_request(
    notification_id bigint primary key,
    sender_id int not null,
    foreign key (notification_id) references notification(id) on delete cascade,
    foreign key (sender_id) references user(id) on delete cascade
);
create table conversation_request(
    notification_id bigint primary key,
    sender_id int not null,
    conversation_id bigint not null,
    foreign key (notification_id) references notification(id) on delete cascade,
    foreign key (sender_id) references user(id) on delete cascade,
    foreign key (conversation_id) references conversation(id) on delete cascade
);
-- rollback drop table conversation_request;
-- rollback drop table friend_request;
-- rollback drop table notification;
-- changeset Fxynos:6
insert into conversation_rights (
    role,
    get_reports,
    edit_data,
    edit_members,
    edit_rights
) values
    ("member", false, false, false, false),
    ("admin", true, true, true, false),
    ("owner", true, true, true, true);
-- rollback delete from conversation_rights;