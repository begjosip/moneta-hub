-- Create table currency
CREATE TABLE IF NOT EXISTS currency
(
    id            BIGSERIAL PRIMARY KEY,
    code          TEXT NOT NULL,
    currency_name TEXT NOT NULL,
    country_code   TEXT NOT NULL
);

ALTER TABLE currency
    ADD CONSTRAINT uc_currency__code UNIQUE (code);

ALTER TABLE currency
    ADD CONSTRAINT uc_currency__currency_name UNIQUE (currency_name);