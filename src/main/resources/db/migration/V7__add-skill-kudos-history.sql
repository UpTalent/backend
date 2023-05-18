ALTER TABLE proof
    ALTER COLUMN kudos TYPE bigint;

ALTER TABLE sponsor
    ALTER COLUMN kudos TYPE bigint;

CREATE TABLE skill_kudos_history (
    id bigserial PRIMARY KEY,
    skill_id bigint REFERENCES skill(id),
    kudos_history_id bigint REFERENCES kudos_history(id),
    kudos bigint
);

ALTER TABLE kudos_history
    ALTER COLUMN kudos TYPE bigint;

ALTER TABLE skill_kudos
    ALTER COLUMN kudos TYPE bigint;

ALTER TABLE skill_kudos
    ADD COLUMN skill_kudos_history_id bigint REFERENCES skill_kudos_history(id);
