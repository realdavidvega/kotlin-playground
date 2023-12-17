CREATE TABLE IF NOT EXISTS persons (
    id SERIAL PRIMARY KEY,
    firstName VARCHAR(255) NOT NULL,
    lastName VARCHAR(255) NOT NULL,
    birthdate DATE,
    CONSTRAINT unique_user UNIQUE (firstName, lastName)
);