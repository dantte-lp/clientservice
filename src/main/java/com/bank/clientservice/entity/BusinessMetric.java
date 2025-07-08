package com.bank.clientservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "business_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "new_clients_count")
    private Integer newClientsCount;

    @Column(name = "deleted_clients_count")
    private Integer deletedClientsCount;

    @Column(name = "updated_clients_count")
    private Integer updatedClientsCount;

    @Column(name = "total_api_calls")
    private Integer totalApiCalls;

    @Column(name = "unique_users_count")
    private Integer uniqueUsersCount;

    @Column(name = "avg_response_time_ms", precision = 10, scale = 2)
    private BigDecimal avgResponseTimeMs;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}