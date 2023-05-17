create table if not exists skill_kudos(
    id serial not null,
    skill_id bigint not null,
    proof_id bigint not null,
    kudos bigint not null default 0,
    primary key (id),
    foreign key (skill_id) references skill(id) on delete cascade,
    foreign key (proof_id) references proof(id) on delete cascade
);
INSERT INTO skill_kudos (skill_id, proof_id, kudos)
SELECT skill_id, proof_id, 0
FROM skill_proof;

drop table if exists skill_proof;