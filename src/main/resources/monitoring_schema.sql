-- Таблица для хранения метрик системы
CREATE TABLE system_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metric_type VARCHAR(50) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(20, 4) NOT NULL,
    unit VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для быстрого поиска
CREATE INDEX idx_system_metrics_timestamp ON system_metrics(timestamp);
CREATE INDEX idx_system_metrics_type_name ON system_metrics(metric_type, metric_name);
CREATE INDEX idx_system_metrics_composite ON system_metrics(metric_type, metric_name, timestamp);

-- Таблица для хранения истории health checks
CREATE TABLE health_check_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
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

CREATE INDEX idx_health_check_timestamp ON health_check_history(timestamp);

-- Таблица для API метрик
CREATE TABLE api_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
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

CREATE INDEX idx_api_metrics_timestamp ON api_metrics(timestamp);
CREATE INDEX idx_api_metrics_endpoint ON api_metrics(endpoint, method);
CREATE INDEX idx_api_metrics_status ON api_metrics(status_code);

-- Таблица для бизнес-метрик
CREATE TABLE business_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
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

CREATE INDEX idx_business_metrics_date ON business_metrics(metric_date);

-- Представление для статистики клиентов по странам
CREATE VIEW client_country_stats AS
SELECT
    citizenship,
    COUNT(*) as client_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) as percentage
FROM clients
GROUP BY citizenship
ORDER BY client_count DESC;

-- Представление для возрастного распределения
CREATE VIEW client_age_distribution AS
SELECT
    CASE
        WHEN age < 18 THEN '0-17'
        WHEN age BETWEEN 18 AND 25 THEN '18-25'
        WHEN age BETWEEN 26 AND 35 THEN '26-35'
        WHEN age BETWEEN 36 AND 45 THEN '36-45'
        WHEN age BETWEEN 46 AND 55 THEN '46-55'
        WHEN age BETWEEN 56 AND 65 THEN '56-65'
        ELSE '65+'
    END as age_group,
    COUNT(*) as client_count
FROM (
    SELECT EXTRACT(YEAR FROM AGE(birth_date)) as age
    FROM clients
) as client_ages
GROUP BY age_group
ORDER BY age_group;

-- Функция для очистки старых метрик (хранить данные за последние 30 дней)
CREATE OR REPLACE FUNCTION cleanup_old_metrics() RETURNS void AS $$
BEGIN
    DELETE FROM system_metrics WHERE timestamp < NOW() - INTERVAL '30 days';
    DELETE FROM health_check_history WHERE timestamp < NOW() - INTERVAL '30 days';
    DELETE FROM api_metrics WHERE timestamp < NOW() - INTERVAL '30 days';
    DELETE FROM business_metrics WHERE metric_date < CURRENT_DATE - INTERVAL '90 days';
END;
$$ LANGUAGE plpgsql;

-- Создать задание для автоматической очистки (если есть pg_cron)
-- SELECT cron.schedule('cleanup-old-metrics', '0 2 * * *', 'SELECT cleanup_old_metrics();');