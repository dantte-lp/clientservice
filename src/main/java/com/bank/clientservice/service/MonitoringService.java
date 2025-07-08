package com.bank.clientservice.service;

import com.bank.clientservice.dto.*;
import com.bank.clientservice.entity.HealthCheckHistory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MonitoringService {

    // Сбор метрик
    void collectSystemMetrics();
    void saveHealthCheckHistory();
    void aggregateBusinessMetrics();

    // Получение данных для дашборда
    DashboardDto getDashboardData();

    // Получение конкретных метрик
    List<MetricDto> getCpuMetrics(LocalDateTime from, LocalDateTime to);
    List<MetricDto> getMemoryMetrics(LocalDateTime from, LocalDateTime to);
    Map<String, Object> getApiStatistics(int hours);
    Map<String, Object> getBusinessMetrics(int days);

    // Системная информация
    SystemInfoDto getSystemInfo();
    Map<String, Object> getExtendedHealthInfo();

    // История health checks
    List<HealthCheckHistory> getHealthHistory(LocalDateTime from, LocalDateTime to);

    // Сводка метрик
    Map<String, Object> getMetricsSummary();

    // Активные алерты
    List<AlertDto> getActiveAlerts();

    // Real-time подписка
    void addMetricSubscriber(SseEmitter emitter);
    void removeMetricSubscriber(SseEmitter emitter);

    // Очистка старых данных
    void cleanupOldMetrics();
}