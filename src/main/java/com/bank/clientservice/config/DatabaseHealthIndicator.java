package com.bank.clientservice.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(2)) {
                DatabaseMetaData metaData = connection.getMetaData();

                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("url", metaData.getURL())
                        .withDetail("databaseProductName", metaData.getDatabaseProductName())
                        .withDetail("databaseProductVersion", metaData.getDatabaseProductVersion())
                        .withDetail("driverName", metaData.getDriverName())
                        .withDetail("driverVersion", metaData.getDriverVersion())
                        .withDetail("connectionValid", true)
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", "Connection validation failed")
                        .withDetail("connectionValid", false)
                        .build();
            }
        } catch (SQLException e) {
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", e.getMessage())
                    .withDetail("errorCode", e.getErrorCode())
                    .withDetail("sqlState", e.getSQLState())
                    .withException(e)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", "Unexpected error: " + e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}