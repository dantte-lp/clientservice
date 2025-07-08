package com.bank.clientservice.dto;

import com.bank.clientservice.entity.BusinessMetric;
import com.bank.clientservice.entity.HealthCheckHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private List<MetricDto> cpuMetrics;
    private List<MetricDto> memoryMetrics;
    private Map<String, Long> apiStatistics;
    private Map<String, Long> countryDistribution;
    private List<HealthCheckHistory> healthCheckHistory;
    private List<BusinessMetric> businessMetrics;
    private Long totalClients;
    private List<AlertDto> activeAlerts;
    private LocalDateTime lastUpdateTime;
}