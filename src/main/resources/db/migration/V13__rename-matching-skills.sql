ALTER TABLE vacancy
    ADD COLUMN skills_matched_percent int NOT NULL DEFAULT 100;

UPDATE vacancy
SET skills_matched_percent = count_matched_skills;

ALTER TABLE vacancy
    DROP COLUMN count_matched_skills;
