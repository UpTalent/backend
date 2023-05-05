create table if not exists skill (
    id serial not null,
    name varchar(30) not null,
    primary key (id)
    );

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

drop table talent_skills

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

create table if not exists skill_proof (
    skill_id bigint not null,
    proof_id bigint not null,
    primary key (skill_id, proof_id),
    foreign key (skill_id) references skill(id) on delete cascade,
    foreign key (proof_id) references proof(id) on delete cascade
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
create table if not exists skill_talent (
    talent_id bigint not null,
    skill_id bigint not null,
    primary key(talent_id, skill_id),
    foreign key (talent_id) references talent(id) on delete cascade,
    foreign key (skill_id) references skill(id) on delete cascade

    )