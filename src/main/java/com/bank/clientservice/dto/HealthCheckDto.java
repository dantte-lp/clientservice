package com.bank.clientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckDto {

    private String status;
    private LocalDateTime timestamp;
    private ServerInfo server;
    private DatabaseInfo database;
    private ApplicationInfo application;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServerInfo {
        private String hostname;
        private String ipAddress;
        private String osName;
        private String osVersion;
        private String javaVersion;
        private long uptime;
        private Map<String, Object> memory;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseInfo {
        private String status;
        private String url;
        private String databaseProductName;
        private String databaseProductVersion;
        private String driverName;
        private String driverVersion;
        private long clientsCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationInfo {
        private String name;
        private String version;
        private String description;
        private LocalDateTime startTime;
    }
}