package com.bank.clientservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "health_check_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "database_status", length = 20)
    private String databaseStatus;

    @Column(name = "response_time_ms")
    private Integer responseTimeMs;

    @Column(name = "total_memory_mb")
    private Integer totalMemoryMb;

    @Column(name = "used_memory_mb")
    private Integer usedMemoryMb;

    @Column(name = "cpu_usage_percent", precision = 5, scale = 2)
    private BigDecimal cpuUsagePercent;

    @Column(name = "active_threads")
    private Integer activeThreads;

    @Column(name = "total_clients")
    private Long totalClients;

    @Column(name = "error_count")
    private Integer errorCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> details;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}