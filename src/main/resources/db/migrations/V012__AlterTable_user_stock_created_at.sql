ALTER TABLE user_stock
    ADD created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;

ALTER TABLE user_stock
    ADD last_modified_at TIMESTAMP NULL;