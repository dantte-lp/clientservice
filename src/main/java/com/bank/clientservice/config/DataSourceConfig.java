package com.bank.clientservice.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);

        // Connection pool settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(5000); // 5 seconds
        config.setValidationTimeout(3000);
        config.setMaxLifetime(600000);

        // Connection test query
        config.setConnectionTestQuery("SELECT 1");

        // Allow pool to start even if DB is down
        config.setInitializationFailTimeout(-1);

        log.info("Creating DataSource with URL: {}", url);

        try {
            HikariDataSource dataSource = new HikariDataSource(config);
            log.info("DataSource created successfully");
            return dataSource;
        } catch (Exception e) {
            log.error("Failed to create DataSource: {}", e.getMessage());
            throw e;
        }
    }
}