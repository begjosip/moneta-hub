-- Create table user stock for user favourite stocks

CREATE TABLE IF NOT EXISTS user_stock
(
    id         BIGSERIAL PRIMARY KEY,
    ticker     TEXT   NOT NULL,
    fk_user_id BIGINT NOT NULL,
    FOREIGN KEY (fk_user_id) REFERENCES moneta_user (id)
);
