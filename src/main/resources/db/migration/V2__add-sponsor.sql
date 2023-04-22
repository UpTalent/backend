drop table if exists kudos_history;
drop table if exists proof;
drop table if exists talent_skills;
drop table if exists talent;

create table if not exists credentials (
    id serial not null,
    email varchar(100) not null,
    password varchar(255) not null,
    status varchar(255) not null,
    role varchar(255),
    primary key (id)
);

create table if not exists talent (
    id serial not null,
    firstname varchar(15) not null,
    lastname varchar(15) not null,
    avatar varchar(255),
    banner varchar(255),
    birthday date,
    about_me varchar(2250),
    location varchar(255),
    primary key (id)
);

create table if not exists talent_skills (
    talent_id bigint not null,
    skills varchar(255) not null
);

create table if not exists talent_credentials (
    talent_id bigint not null references talent(id) on delete cascade,
    credentials_id bigint not null references credentials(id) on delete cascade,
    primary key (talent_id)
);


create table if not exists proof (
    id serial not null,
    icon_number integer not null,
    title varchar(255) not null,
    summary varchar(255) not null,
    content varchar(5000) not null,
    published timestamp(6),
    status varchar(255) not null,
    kudos integer not null default 0,
    talent_id bigint not null references talent(id) on delete cascade,
    primary key (id)
);

create table if not exists sponsor (
    id serial not null,
    fullname varchar(30) not null,
    avatar varchar(255),
    kudos integer,
    expiration_deleting timestamp(6),
    primary key (id)
);

create table if not exists kudos_history (
    id serial not null,
    kudos integer not null,
    sent timestamp(6) not null,
    sponsor_id bigint not null,
    proof_id bigint not null references proof(id) on delete cascade,
    primary key (id)
);

create table if not exists sponsor_credentials (
    sponsor_id bigint not null references sponsor(id) on delete cascade,
    credentials_id bigint not null references credentials(id) on delete cascade,
    primary key (sponsor_id)
);

alter table talent_skills
    add constraint fk_talent_skills_talent
        foreign key (talent_id)
            references talent(id)
            on delete cascade;

alter table proof
    add constraint fk_proof_talent
        foreign key (talent_id)
            references talent(id)
            on delete cascade;

alter table kudos_history
    add constraint fk_kudos_history_sponsor
        foreign key (sponsor_id)
            references sponsor(id)
            on delete cascade;