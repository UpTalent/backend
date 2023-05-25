ALTER TABLE skill_kudos_history
    DROP CONSTRAINT skill_kudos_history_kudos_history_id_fkey;
ALTER TABLE skill_kudos_history
    DROP CONSTRAINT skill_kudos_history_skill_id_fkey;
ALTER TABLE skill_kudos_history ADD CONSTRAINT skill_kudos_history_kudos_history_id_fkey
    foreign key (kudos_history_id) REFERENCES kudos_history(id) on delete cascade on update cascade;
ALTER TABLE skill_kudos_history ADD CONSTRAINT skill_kudos_history_skill_id_fkey
    foreign key (skill_id) REFERENCES skill(id) on delete cascade on update cascade;
