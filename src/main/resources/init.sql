-- Create database
CREATE DATABASE IF NOT EXISTS clientdb
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Connect to clientdb
\c clientdb;

-- Create clients table (will be auto-created by Hibernate, but this is for reference)
CREATE TABLE IF NOT EXISTS clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    last_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    birth_date DATE NOT NULL,
    client_number VARCHAR(50) UNIQUE NOT NULL,
    citizenship VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index for better search performance
CREATE INDEX IF NOT EXISTS idx_clients_last_name ON clients(last_name);
CREATE INDEX IF NOT EXISTS idx_clients_first_name ON clients(first_name);
CREATE INDEX IF NOT EXISTS idx_clients_client_number ON clients(client_number);
CREATE INDEX IF NOT EXISTS idx_clients_citizenship ON clients(citizenship);

-- Insert sample data (optional)
INSERT INTO clients (id, last_name, first_name, middle_name, birth_date, client_number, citizenship, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'Иванов', 'Иван', 'Иванович', '1990-05-15', 'CL-20250107-0001', 'Российская Федерация', NOW(), NOW()),
    (gen_random_uuid(), 'Петров', 'Петр', 'Петрович', '1985-08-22', 'CL-20250107-0002', 'Республика Беларусь', NOW(), NOW()),
    (gen_random_uuid(), 'Сидорова', 'Елена', 'Александровна', '1992-11-30', 'CL-20250107-0003', 'Республика Казахстан', NOW(), NOW())
ON CONFLICT (client_number) DO NOTHING;