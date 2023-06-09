create table if not exists vacancy(
    id serial not null,
    title varchar(255) not null,
    content varchar(3000) not null,
    published timestamp,
    sponsor_id bigint not null,
    primary key (id),
    foreign key (sponsor_id) references sponsor(id) on delete cascade
);

create table if not exists skill_vacancy(
    skill_id bigint not null,
    vacancy_id bigint not null,
    primary key (skill_id, vacancy_id),
    foreign key (skill_id) references skill(id) on delete cascade,
    foreign key (vacancy_id) references vacancy(id) on delete cascade
);