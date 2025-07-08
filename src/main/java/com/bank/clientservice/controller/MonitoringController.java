package com.bank.clientservice.controller;

import com.bank.clientservice.dto.DashboardDto;
import com.bank.clientservice.dto.MetricDto;
import com.bank.clientservice.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Controller
@RequestMapping("/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    // Веб-интерфейс дашборда
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        DashboardDto dashboardData = monitoringService.getDashboardData();
        model.addAttribute("dashboard", dashboardData);
        model.addAttribute("pageTitle", "Monitoring Dashboard");
        return "monitoring/dashboard";
    }

    // API endpoints для получения метрик
    @GetMapping("/api/metrics/cpu")
    @ResponseBody
    public ResponseEntity<List<MetricDto>> getCpuMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        if (from == null) from = LocalDateTime.now().minusHours(1);
        if (to == null) to = LocalDateTime.now();

        List<MetricDto> metrics = monitoringService.getCpuMetrics(from, to);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/api/metrics/memory")
    @ResponseBody
    public ResponseEntity<List<MetricDto>> getMemoryMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        if (from == null) from = LocalDateTime.now().minusHours(1);
        if (to == null) to = LocalDateTime.now();

        List<MetricDto> metrics = monitoringService.getMemoryMetrics(from, to);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/api/metrics/api-stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getApiStatistics(
            @RequestParam(defaultValue = "24") int hours) {

        Map<String, Object> stats = monitoringService.getApiStatistics(hours);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/metrics/business")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBusinessMetrics(
            @RequestParam(defaultValue = "7") int days) {

        Map<String, Object> metrics = monitoringService.getBusinessMetrics(days);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/api/health/extended")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getExtendedHealthInfo() {
        Map<String, Object> info = monitoringService.getExtendedHealthInfo();
        return ResponseEntity.ok(info);
    }

    // Real-time метрики через Server-Sent Events
    @GetMapping("/api/metrics/stream")
    public SseEmitter streamMetrics() {
        // Create emitter with 30 minute timeout
        SseEmitter emitter = new SseEmitter(30L * 60 * 1000);

        // Add to subscribers
        monitoringService.addMetricSubscriber(emitter);

        // Handle cleanup on timeout/error/completion
        emitter.onCompletion(() -> {
            log.debug("SSE connection completed");
            monitoringService.removeMetricSubscriber(emitter);
        });

        emitter.onTimeout(() -> {
            log.debug("SSE connection timeout");
            monitoringService.removeMetricSubscriber(emitter);
        });

        emitter.onError(e -> {
            log.debug("SSE connection error: {}", e.getMessage());
            monitoringService.removeMetricSubscriber(emitter);
        });

        return emitter;
    }
}