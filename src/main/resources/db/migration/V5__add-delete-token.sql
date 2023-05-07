create table if not exists credentials (
    id serial not null,
    email varchar(100) not null,
    password varchar(255) not null,
    status varchar(255) not null,
    role varchar(255),
    delete_token varchar(255),
    expiration_deleting timestamp(6),
    primary key (id)
);

create table if not exists sponsor (
    id serial not null,
    fullname varchar(30) not null,
    avatar varchar(255),
    kudos integer,
    primary key (id)
);