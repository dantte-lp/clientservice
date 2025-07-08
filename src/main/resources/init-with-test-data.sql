-- Создание базы данных
CREATE DATABASE IF NOT EXISTS clientdb
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;

-- Подключение к базе
\c clientdb;

-- Включение расширения для генерации UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Создание таблицы клиентов (если еще не создана Hibernate)
CREATE TABLE IF NOT EXISTS clients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    last_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    birth_date DATE NOT NULL,
    client_number VARCHAR(50) UNIQUE NOT NULL,
    citizenship VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Создание индексов для улучшения производительности
CREATE INDEX IF NOT EXISTS idx_clients_last_name ON clients(last_name);
CREATE INDEX IF NOT EXISTS idx_clients_first_name ON clients(first_name);
CREATE INDEX IF NOT EXISTS idx_clients_client_number ON clients(client_number);
CREATE INDEX IF NOT EXISTS idx_clients_citizenship ON clients(citizenship);
CREATE INDEX IF NOT EXISTS idx_clients_created_at ON clients(created_at);

-- Вставка тестовых данных
INSERT INTO clients (id, last_name, first_name, middle_name, birth_date, client_number, citizenship, created_at, updated_at)
VALUES
    (uuid_generate_v4(), 'Иванов', 'Иван', 'Иванович', '1990-05-15', 'CL-20250107-0001', 'Российская Федерация', NOW(), NOW()),
    (uuid_generate_v4(), 'Петров', 'Петр', 'Петрович', '1985-08-22', 'CL-20250107-0002', 'Республика Беларусь', NOW(), NOW()),
    (uuid_generate_v4(), 'Сидорова', 'Елена', 'Александровна', '1992-11-30', 'CL-20250107-0003', 'Республика Казахстан', NOW(), NOW()),
    (uuid_generate_v4(), 'Козлов', 'Андрей', 'Сергеевич', '1988-03-10', 'CL-20250107-0004', 'Российская Федерация', NOW(), NOW()),
    (uuid_generate_v4(), 'Новикова', 'Ольга', 'Владимировна', '1995-07-25', 'CL-20250107-0005', 'Украина', NOW(), NOW()),
    (uuid_generate_v4(), 'Морозов', 'Дмитрий', 'Алексеевич', '1991-01-18', 'CL-20250107-0006', 'Российская Федерация', NOW(), NOW()),
    (uuid_generate_v4(), 'Волкова', 'Анна', 'Николаевна', '1987-09-05', 'CL-20250107-0007', 'Республика Беларусь', NOW(), NOW()),
    (uuid_generate_v4(), 'Соколов', 'Михаил', 'Павлович', '1993-12-12', 'CL-20250107-0008', 'Республика Казахстан', NOW(), NOW()),
    (uuid_generate_v4(), 'Попова', 'Татьяна', 'Игоревна', '1989-04-28', 'CL-20250107-0009', 'Российская Федерация', NOW(), NOW()),
    (uuid_generate_v4(), 'Лебедев', 'Сергей', 'Викторович', '1994-06-03', 'CL-20250107-0010', 'Республика Узбекистан', NOW(), NOW()),
    (uuid_generate_v4(), 'Кузнецова', 'Мария', 'Андреевна', '1986-10-20', 'CL-20250107-0011', 'Российская Федерация', NOW(), NOW()),
    (uuid_generate_v4(), 'Смирнов', 'Александр', 'Дмитриевич', '1990-02-14', 'CL-20250107-0012', 'Республика Армения', NOW(), NOW()),
    (uuid_generate_v4(), 'Михайлова', 'Екатерина', 'Сергеевна', '1991-08-07', 'CL-20250107-0013', 'Российская Федерация', NOW(), NOW()),
    (uuid_generate_v4(), 'Федоров', 'Николай', 'Александрович', '1988-11-23', 'CL-20250107-0014', 'Республика Молдова', NOW(), NOW()),
    (uuid_generate_v4(), 'Алексеева', 'Светлана', 'Петровна', '1992-05-16', 'CL-20250107-0015', 'Российская Федерация', NOW(), NOW())
ON CONFLICT (client_number) DO NOTHING;

-- Создание таблиц для мониторинга
CREATE TABLE IF NOT EXISTS system_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metric_type VARCHAR(50) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(20, 4) NOT NULL,
    unit VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_system_metrics_timestamp ON system_metrics(timestamp);
CREATE INDEX IF NOT EXISTS idx_system_metrics_type_name ON system_metrics(metric_type, metric_name);

CREATE TABLE IF NOT EXISTS health_check_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    database_status VARCHAR(20),
    response_time_ms INTEGER,
    total_memory_mb INTEGER,
    used_memory_mb INTEGER,
    cpu_usage_percent DECIMAL(5, 2),
    active_threads INTEGER,
    total_clients BIGINT,
    error_count INTEGER DEFAULT 0,
    details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_health_check_timestamp ON health_check_history(timestamp);

CREATE TABLE IF NOT EXISTS api_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    endpoint VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    status_code INTEGER NOT NULL,
    response_time_ms INTEGER NOT NULL,
    request_size_bytes BIGINT,
    response_size_bytes BIGINT,
    user_agent VARCHAR(500),
    ip_address VARCHAR(45),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_api_metrics_timestamp ON api_metrics(timestamp);
CREATE INDEX IF NOT EXISTS idx_api_metrics_endpoint ON api_metrics(endpoint, method);

CREATE TABLE IF NOT EXISTS business_metrics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metric_date DATE NOT NULL,
    new_clients_count INTEGER DEFAULT 0,
    deleted_clients_count INTEGER DEFAULT 0,
    updated_clients_count INTEGER DEFAULT 0,
    total_api_calls INTEGER DEFAULT 0,
    unique_users_count INTEGER DEFAULT 0,
    avg_response_time_ms DECIMAL(10, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_business_metrics_date ON business_metrics(metric_date);

-- Вывод статистики
SELECT
    'Clients' as table_name,
    COUNT(*) as record_count
FROM clients
UNION ALL
SELECT
    'Countries represented' as table_name,
    COUNT(DISTINCT citizenship) as record_count
FROM clients;

-- Проверка создания всех таблиц
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;