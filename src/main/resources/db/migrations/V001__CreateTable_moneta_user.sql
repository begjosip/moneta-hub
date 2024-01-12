-- Create table moneta_user for user credentials

CREATE TABLE IF NOT EXISTS moneta_user
(
    id               BIGSERIAL PRIMARY KEY,
    first_name       TEXT                                NOT NULL,
    last_name        TEXT                                NOT NULL,
    email            TEXT                                NOT NULL,
    password         TEXT                                NOT NULL,
    status           TEXT                                NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_modified_at TIMESTAMP                           NULL
);

ALTER TABLE moneta_user
    ADD CONSTRAINT uc_moneta_user__email UNIQUE (email);