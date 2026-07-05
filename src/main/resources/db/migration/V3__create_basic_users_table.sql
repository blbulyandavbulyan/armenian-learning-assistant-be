CREATE TABLE users
(
    id  UUID PRIMARY KEY,
    issuer VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    UNIQUE (issuer, subject)
);
