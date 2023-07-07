CREATE TABLE submission
(
    id SERIAL PRIMARY KEY,
    contact_info VARCHAR(100) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    sent TIMESTAMPTZ NOT NULL,
    vacancy_id BIGINT,
    talent_id BIGINT,
    FOREIGN KEY (vacancy_id) REFERENCES vacancy (id),
    FOREIGN KEY (talent_id) REFERENCES talent (id)
);