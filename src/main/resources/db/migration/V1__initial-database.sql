drop table if exists kudos_history;
drop table if exists proof;
drop table if exists talent_skills;
drop table if exists talent;

create table if not exists talent (
     id serial not null,
     about_me varchar(2250),
     avatar varchar(255),
     banner varchar(255),
     birthday date,
     email varchar(100) not null,
     firstname varchar(15) not null,
     lastname varchar(15) not null,
     location varchar(255),
     password varchar(255) not null,
     primary key (id)
);


create table if not exists talent_skills (
     talent_id bigint not null,
     skills varchar(255) not null
);

create table if not exists proof (
     id serial not null,
     content varchar(5000) not null,
     icon_number integer not null,
     kudos integer not null default 0,
     published timestamp(6),
     status varchar(255) not null,
     summary varchar(255) not null,
     title varchar(255) not null,
     talent_id bigint not null,
     primary key (id)
);

create table if not exists kudos_history (
    id serial not null,
    kudos integer not null,
    sent timestamp(6) not null,
    proof_id bigint not null,
    talent_id bigint not null,
    primary key (id)
);

alter table kudos_history
    add constraint fk_kudos_history_proof
    foreign key (proof_id)
    references proof(id)
    on delete cascade;

alter table kudos_history
    add constraint fk_kudos_history_talent
    foreign key (talent_id)
    references talent(id)
    on delete cascade;

alter table proof
    add constraint fk_proof_talent
    foreign key (talent_id)
    references talent(id)
    on delete cascade;

alter table talent_skills
    add constraint fk_talent_skills_talent
    foreign key (talent_id)
    references talent(id)
    on delete cascade;


