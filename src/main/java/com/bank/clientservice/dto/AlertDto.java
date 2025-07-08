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
public class AlertDto {
    private String id;
    private AlertType type;
    private AlertSeverity severity;
    private String title;
    private String message;
    private LocalDateTime timestamp;
    private boolean acknowledged;
    private Map<String, Object> metadata;

    public enum AlertType {
        CPU_HIGH,
        MEMORY_HIGH,
        DISK_SPACE_LOW,
        DATABASE_CONNECTION_FAILED,
        RESPONSE_TIME_HIGH,
        ERROR_RATE_HIGH,
        CUSTOM
    }

    public enum AlertSeverity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }
}