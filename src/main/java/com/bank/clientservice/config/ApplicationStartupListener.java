package com.bank.clientservice.config;

import com.bank.clientservice.service.DatabaseAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    private final DatabaseAvailabilityService databaseAvailabilityService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Application started successfully");

        if (databaseAvailabilityService.isDatabaseAvailable()) {
            log.info("Database connection is available");
        } else {
            log.warn("WARNING: Database connection is NOT available!");
            log.warn("The application is running in degraded mode");
            log.warn("Some features will not be available until database connection is restored");
        }
    }
}