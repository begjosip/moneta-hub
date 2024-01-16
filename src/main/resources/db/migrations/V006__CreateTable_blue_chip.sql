-- Create table blue_chip for most reliable stocks

CREATE TABLE IF NOT EXISTS blue_chip
(
    id           BIGSERIAL PRIMARY KEY,
    ticker       TEXT NOT NULL,
    company_name TEXT NOT NULL
);

ALTER TABLE blue_chip
    ADD CONSTRAINT uc_blue_chip__ticker UNIQUE (ticker);

ALTER TABLE blue_chip
    ADD CONSTRAINT uc_blue_chip__company_name UNIQUE (company_name);