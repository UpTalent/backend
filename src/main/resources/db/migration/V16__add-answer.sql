CREATE TABLE answer
(
    id SERIAL PRIMARY KEY,
    contact_info VARCHAR(100),
    message VARCHAR(1000) NOT NULL,
    title VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    sponsor_id BIGINT,
    is_templated_message BOOLEAN,
    FOREIGN KEY (sponsor_id) REFERENCES sponsor (id)
);