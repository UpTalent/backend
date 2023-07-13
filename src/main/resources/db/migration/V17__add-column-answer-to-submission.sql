ALTER TABLE submission
    ADD COLUMN answer_id bigint not null;
ALTER TABLE submission ADD CONSTRAINT answer_submission_id_fkey
    foreign key (answer_id) REFERENCES answer(id) on delete cascade on update cascade;