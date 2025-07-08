package com.bank.clientservice.controller;

import com.bank.clientservice.dto.AlertDto;
import com.bank.clientservice.dto.SystemInfoDto;
import com.bank.clientservice.entity.HealthCheckHistory;
import com.bank.clientservice.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MonitoringRestController {

    private final MonitoringService monitoringService;

    @GetMapping("/system-info")
    public ResponseEntity<SystemInfoDto> getSystemInfo() {
        SystemInfoDto systemInfo = monitoringService.getSystemInfo();
        return ResponseEntity.ok(systemInfo);
    }

    @GetMapping("/health-history")
    public ResponseEntity<List<HealthCheckHistory>> getHealthHistory(
            @RequestParam(defaultValue = "24") int hours) {

        LocalDateTime from = LocalDateTime.now().minusHours(hours);
        List<HealthCheckHistory> history = monitoringService.getHealthHistory(from, LocalDateTime.now());
        return ResponseEntity.ok(history);
    }

    @GetMapping("/metrics/summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        Map<String, Object> summary = monitoringService.getMetricsSummary();
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/metrics/collect")
    public ResponseEntity<Void> triggerMetricsCollection() {
        monitoringService.collectSystemMetrics();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AlertDto>> getActiveAlerts() {
        List<AlertDto> alerts = monitoringService.getActiveAlerts();
        return ResponseEntity.ok(alerts);
    }
}