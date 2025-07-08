package com.bank.clientservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseAvailabilityService {

    private final DataSource dataSource;
    private volatile boolean lastKnownStatus = false;
    private volatile long lastCheckTime = 0;
    private static final long CHECK_INTERVAL = 5000; // 5 seconds

    public boolean isDatabaseAvailable() {
        long currentTime = System.currentTimeMillis();

        // Use cached result if checked recently
        if (currentTime - lastCheckTime < CHECK_INTERVAL) {
            return lastKnownStatus;
        }

        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(2);

            if (isValid && !lastKnownStatus) {
                log.info("Database connection restored");
            } else if (!isValid && lastKnownStatus) {
                log.warn("Database connection lost");
            }

            lastKnownStatus = isValid;
            lastCheckTime = currentTime;
            return isValid;
        } catch (SQLException e) {
            log.debug("Database connection check failed: {}", e.getMessage());
            lastKnownStatus = false;
            lastCheckTime = currentTime;
            return false;
        }
    }

    public String getDatabaseStatus() {
        return isDatabaseAvailable() ? "UP" : "DOWN";
    }

    public String getDatabaseError() {
        if (isDatabaseAvailable()) {
            return null;
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.isValid(2);
            return null;
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}