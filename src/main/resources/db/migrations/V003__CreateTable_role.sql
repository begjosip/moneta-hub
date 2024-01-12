-- Create table role for user roles

CREATE TABLE IF NOT EXISTS role
(
    id   BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL
);