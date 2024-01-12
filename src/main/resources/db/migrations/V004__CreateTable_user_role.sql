-- Create table user_role for joined table

CREATE TABLE IF NOT EXISTS user_role
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES moneta_user (id),
    FOREIGN KEY (role_id) REFERENCES role (id)
);