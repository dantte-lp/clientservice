package com.bank.clientservice.service.impl;

import com.bank.clientservice.dto.*;
import com.bank.clientservice.entity.*;
import com.bank.clientservice.repository.*;
import com.bank.clientservice.service.MonitoringService;
import com.sun.management.OperatingSystemMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.IOException;
import java.lang.management.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MonitoringServiceImpl implements MonitoringService {

    private final SystemMetricRepository systemMetricRepository;
    private final HealthCheckHistoryRepository healthCheckHistoryRepository;
    private final ApiMetricRepository apiMetricRepository;
    private final BusinessMetricRepository businessMetricRepository;
    private final ClientRepository clientRepository;

    // Список подписчиков для real-time обновлений
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private static final LocalDateTime START_TIME = LocalDateTime.now();

    // Сбор метрик каждую минуту
    @Override
    @Scheduled(fixedDelay = 60000)
    public void collectSystemMetrics() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<SystemMetric> metrics = new ArrayList<>();

            // CPU метрики
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double cpuLoad = osBean.getProcessCpuLoad() * 100;
            metrics.add(createMetric(now, "CPU", "process_cpu_usage", cpuLoad, "%"));
            metrics.add(createMetric(now, "CPU", "system_cpu_usage", osBean.getSystemCpuLoad() * 100, "%"));
            metrics.add(createMetric(now, "CPU", "available_processors", osBean.getAvailableProcessors(), "cores"));

            // Memory метрики
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory() / (1024 * 1024);
            long freeMemory = runtime.freeMemory() / (1024 * 1024);
            long usedMemory = totalMemory - freeMemory;
            long maxMemory = runtime.maxMemory() / (1024 * 1024);

            metrics.add(createMetric(now, "MEMORY", "heap_used", usedMemory, "MB"));
            metrics.add(createMetric(now, "MEMORY", "heap_total", totalMemory, "MB"));
            metrics.add(createMetric(now, "MEMORY", "heap_max", maxMemory, "MB"));
            metrics.add(createMetric(now, "MEMORY", "heap_usage_percent",
                    (double) usedMemory / totalMemory * 100, "%"));

            // Thread метрики
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            metrics.add(createMetric(now, "THREADS", "thread_count", threadBean.getThreadCount(), "threads"));
            metrics.add(createMetric(now, "THREADS", "peak_thread_count", threadBean.getPeakThreadCount(), "threads"));
            metrics.add(createMetric(now, "THREADS", "daemon_thread_count", threadBean.getDaemonThreadCount(), "threads"));

            // GC метрики
            ManagementFactory.getGarbageCollectorMXBeans().forEach(gc -> {
                metrics.add(createMetric(now, "GC", gc.getName() + "_count", gc.getCollectionCount(), "times"));
                metrics.add(createMetric(now, "GC", gc.getName() + "_time", gc.getCollectionTime(), "ms"));
            });

            // Database метрики
            long clientCount = clientRepository.count();
            metrics.add(createMetric(now, "DATABASE", "total_clients", clientCount, "clients"));

            systemMetricRepository.saveAll(metrics);
            log.debug("Collected {} system metrics", metrics.size());

        } catch (Exception e) {
            log.error("Error collecting system metrics", e);
        }
    }

    // Сохранение health check каждые 5 минут
    @Override
    @Scheduled(fixedDelay = 300000)
    public void saveHealthCheckHistory() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            Runtime runtime = Runtime.getRuntime();

            HealthCheckHistory history = HealthCheckHistory.builder()
                    .timestamp(LocalDateTime.now())
                    .status("UP")
                    .databaseStatus("UP")
                    .totalMemoryMb((int)(runtime.totalMemory() / (1024 * 1024)))
                    .usedMemoryMb((int)((runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)))
                    .cpuUsagePercent(BigDecimal.valueOf(osBean.getProcessCpuLoad() * 100)
                            .setScale(2, RoundingMode.HALF_UP))
                    .activeThreads(Thread.activeCount())
                    .totalClients(clientRepository.count())
                    .errorCount(0)
                    .details(new HashMap<>())
                    .build();

            healthCheckHistoryRepository.save(history);
            log.debug("Saved health check history");

        } catch (Exception e) {
            log.error("Error saving health check history", e);
        }
    }

    // Агрегация бизнес-метрик каждый час
    @Override
    @Scheduled(cron = "0 0 * * * *")
    public void aggregateBusinessMetrics() {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime now = LocalDateTime.now();

            // Подсчет новых клиентов за день
            long newClientsCount = clientRepository.countByCreatedAtBetween(startOfDay, now);

            // Подсчет API вызовов
            long apiCallsCount = apiMetricRepository.countByTimestampBetween(startOfDay, now);

            // Среднее время ответа
            Double avgResponseTime = apiMetricRepository.findAverageResponseTimeBetween(startOfDay, now);

            BusinessMetric metric = BusinessMetric.builder()
                    .timestamp(now)
                    .metricDate(today)
                    .newClientsCount((int) newClientsCount)
                    .totalApiCalls((int) apiCallsCount)
                    .avgResponseTimeMs(avgResponseTime != null ?
                            BigDecimal.valueOf(avgResponseTime).setScale(2, RoundingMode.HALF_UP) :
                            BigDecimal.ZERO)
                    .build();

            businessMetricRepository.save(metric);
            log.info("Aggregated business metrics for {}", today);

        } catch (Exception e) {
            log.error("Error aggregating business metrics", e);
        }
    }

    @Override
    public DashboardDto getDashboardData() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);
        LocalDateTime oneDayAgo = now.minusDays(1);
        LocalDateTime oneWeekAgo = now.minusWeeks(1);

        // Получение последних метрик
        List<SystemMetric> recentMetrics = systemMetricRepository
                .findByTimestampBetweenOrderByTimestampDesc(oneHourAgo, now);

        // CPU график (последний час)
        List<MetricDto> cpuMetrics = recentMetrics.stream()
                .filter(m -> "CPU".equals(m.getMetricType()) && "process_cpu_usage".equals(m.getMetricName()))
                .map(this::toMetricDto)
                .collect(Collectors.toList());

        // Memory график (последний час)
        List<MetricDto> memoryMetrics = recentMetrics.stream()
                .filter(m -> "MEMORY".equals(m.getMetricType()) && "heap_usage_percent".equals(m.getMetricName()))
                .map(this::toMetricDto)
                .collect(Collectors.toList());

        // API статистика
        Map<String, Long> apiStats = apiMetricRepository.findByTimestampBetween(oneDayAgo, now).stream()
                .collect(Collectors.groupingBy(
                        api -> api.getEndpoint() + " " + api.getMethod(),
                        Collectors.counting()
                ));

        // Статистика по странам
        List<Object[]> countryStats = clientRepository.findClientCountByCountry();

        // История health checks
        List<HealthCheckHistory> healthHistory = healthCheckHistoryRepository
                .findByTimestampBetweenOrderByTimestampDesc(oneDayAgo, now);

        // Бизнес метрики за неделю
        List<BusinessMetric> businessMetrics = businessMetricRepository
                .findByMetricDateBetween(oneWeekAgo.toLocalDate(), now.toLocalDate());

        return DashboardDto.builder()
                .cpuMetrics(cpuMetrics)
                .memoryMetrics(memoryMetrics)
                .apiStatistics(apiStats)
                .countryDistribution(convertCountryStats(countryStats))
                .healthCheckHistory(healthHistory)
                .businessMetrics(businessMetrics)
                .totalClients(clientRepository.count())
                .activeAlerts(getActiveAlerts())
                .lastUpdateTime(now)
                .build();
    }

    @Override
    public List<MetricDto> getCpuMetrics(LocalDateTime from, LocalDateTime to) {
        return systemMetricRepository
                .findByMetricTypeAndMetricNameAndTimestampBetween("CPU", "process_cpu_usage", from, to)
                .stream()
                .map(this::toMetricDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MetricDto> getMemoryMetrics(LocalDateTime from, LocalDateTime to) {
        return systemMetricRepository
                .findByMetricTypeAndMetricNameAndTimestampBetween("MEMORY", "heap_usage_percent", from, to)
                .stream()
                .map(this::toMetricDto)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getApiStatistics(int hours) {
        LocalDateTime from = LocalDateTime.now().minusHours(hours);
        LocalDateTime to = LocalDateTime.now();

        Map<String, Object> stats = new HashMap<>();

        // Endpoint statistics
        List<Object[]> endpointStats = apiMetricRepository.findEndpointStatistics(from, to);
        List<ApiEndpointStatsDto> endpoints = endpointStats.stream()
                .map(row -> ApiEndpointStatsDto.builder()
                        .endpoint((String) row[0])
                        .method((String) row[1])
                        .totalCalls((Long) row[2])
                        .averageResponseTime((Double) row[3])
                        .minResponseTime((Double) row[4])
                        .maxResponseTime((Double) row[5])
                        .build())
                .collect(Collectors.toList());

        // Status code distribution
        List<Object[]> statusStats = apiMetricRepository.findStatusCodeDistribution(from, to);
        Map<Integer, Long> statusCodes = statusStats.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (Long) row[1]
                ));

        stats.put("endpoints", endpoints);
        stats.put("statusCodes", statusCodes);
        stats.put("totalRequests", apiMetricRepository.countByTimestampBetween(from, to));
        stats.put("averageResponseTime", apiMetricRepository.findAverageResponseTimeBetween(from, to));

        return stats;
    }

    @Override
    public Map<String, Object> getBusinessMetrics(int days) {
        LocalDate from = LocalDate.now().minusDays(days);
        LocalDate to = LocalDate.now();

        Map<String, Object> metrics = new HashMap<>();

        List<BusinessMetric> dailyMetrics = businessMetricRepository.findByMetricDateBetween(from, to);
        Long totalNewClients = businessMetricRepository.sumNewClientsBetween(from, to);

        metrics.put("dailyMetrics", dailyMetrics);
        metrics.put("totalNewClients", totalNewClients != null ? totalNewClients : 0);
        metrics.put("averageNewClientsPerDay",
                totalNewClients != null ? totalNewClients / (double) days : 0);

        return metrics;
    }

    @Override
    public SystemInfoDto getSystemInfo() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

            // Disk space
            File root = new File("/");
            SystemInfoDto.DiskSpaceInfo diskSpace = SystemInfoDto.DiskSpaceInfo.builder()
                    .totalSpace(root.getTotalSpace())
                    .freeSpace(root.getFreeSpace())
                    .usableSpace(root.getUsableSpace())
                    .usagePercent((root.getTotalSpace() - root.getFreeSpace()) * 100.0 / root.getTotalSpace())
                    .build();

            // Network info
            List<String> ipAddresses = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    Enumeration<InetAddress> addresses = ni.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ipAddresses.add(addresses.nextElement().getHostAddress());
                    }
                }
            }

            SystemInfoDto.NetworkInfo networkInfo = SystemInfoDto.NetworkInfo.builder()
                    .ipAddresses(ipAddresses)
                    .build();

            return SystemInfoDto.builder()
                    .hostname(localhost.getHostName())
                    .ipAddress(localhost.getHostAddress())
                    .osName(System.getProperty("os.name"))
                    .osVersion(System.getProperty("os.version"))
                    .osArchitecture(System.getProperty("os.arch"))
                    .availableProcessors(osBean.getAvailableProcessors())
                    .javaVersion(System.getProperty("java.version"))
                    .javaVendor(System.getProperty("java.vendor"))
                    .javaHome(System.getProperty("java.home"))
                    .startTime(START_TIME)
                    .uptimeMillis(System.currentTimeMillis() - START_TIME.toInstant(ZoneOffset.UTC).toEpochMilli())
                    .diskSpace(diskSpace)
                    .network(networkInfo)
                    .build();

        } catch (Exception e) {
            log.error("Error getting system info", e);
            return SystemInfoDto.builder().build();
        }
    }

    @Override
    public Map<String, Object> getExtendedHealthInfo() {
        Map<String, Object> extendedInfo = new HashMap<>();

        try {
            // Disk space info
            File[] roots = File.listRoots();
            List<Map<String, Object>> diskInfo = new ArrayList<>();
            for (File root : roots) {
                Map<String, Object> disk = new HashMap<>();
                disk.put("path", root.getAbsolutePath());
                disk.put("total_gb", root.getTotalSpace() / (1024.0 * 1024 * 1024));
                disk.put("free_gb", root.getFreeSpace() / (1024.0 * 1024 * 1024));
                disk.put("usable_gb", root.getUsableSpace() / (1024.0 * 1024 * 1024));
                disk.put("usage_percent",
                        (root.getTotalSpace() - root.getFreeSpace()) * 100.0 / root.getTotalSpace());
                diskInfo.add(disk);
            }
            extendedInfo.put("disk_space", diskInfo);

            // JVM details
            Map<String, Object> jvmInfo = new HashMap<>();
            jvmInfo.put("java_vendor", System.getProperty("java.vendor"));
            jvmInfo.put("java_runtime", System.getProperty("java.runtime.name"));
            jvmInfo.put("java_vm_name", System.getProperty("java.vm.name"));
            jvmInfo.put("java_vm_vendor", System.getProperty("java.vm.vendor"));
            jvmInfo.put("java_home", System.getProperty("java.home"));
            jvmInfo.put("user_timezone", System.getProperty("user.timezone"));
            jvmInfo.put("file_encoding", System.getProperty("file.encoding"));
            extendedInfo.put("jvm_details", jvmInfo);

            // Class loading info
            ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
            Map<String, Object> classInfo = new HashMap<>();
            classInfo.put("loaded_class_count", classLoadingBean.getLoadedClassCount());
            classInfo.put("total_loaded_class_count", classLoadingBean.getTotalLoadedClassCount());
            classInfo.put("unloaded_class_count", classLoadingBean.getUnloadedClassCount());
            extendedInfo.put("class_loading", classInfo);

            // Network interfaces
            List<Map<String, Object>> networkInfo = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    Map<String, Object> netInfo = new HashMap<>();
                    netInfo.put("name", ni.getName());
                    netInfo.put("display_name", ni.getDisplayName());
                    netInfo.put("is_virtual", ni.isVirtual());
                    netInfo.put("mtu", ni.getMTU());

                    List<String> addresses = new ArrayList<>();
                    Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        addresses.add(inetAddresses.nextElement().getHostAddress());
                    }
                    netInfo.put("addresses", addresses);
                    networkInfo.add(netInfo);
                }
            }
            extendedInfo.put("network_interfaces", networkInfo);

        } catch (Exception e) {
            log.error("Error collecting extended health info", e);
            extendedInfo.put("error", e.getMessage());
        }

        return extendedInfo;
    }

    @Override
    public List<HealthCheckHistory> getHealthHistory(LocalDateTime from, LocalDateTime to) {
        return healthCheckHistoryRepository.findByTimestampBetweenOrderByTimestampDesc(from, to);
    }

    @Override
    public Map<String, Object> getMetricsSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourAgo = now.minusHours(1);

        Map<String, Object> summary = new HashMap<>();

        // Current metrics
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        Runtime runtime = Runtime.getRuntime();

        summary.put("cpuUsage", osBean.getProcessCpuLoad() * 100);
        summary.put("memoryUsage", (runtime.totalMemory() - runtime.freeMemory()) * 100.0 / runtime.totalMemory());
        summary.put("activeThreads", Thread.activeCount());
        summary.put("totalClients", clientRepository.count());
        summary.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());

        // Recent averages
        Double avgCpu = systemMetricRepository.findAverageValue("CPU", "process_cpu_usage", oneHourAgo, now);
        Double avgMemory = systemMetricRepository.findAverageValue("MEMORY", "heap_usage_percent", oneHourAgo, now);

        summary.put("avgCpuLastHour", avgCpu != null ? avgCpu : 0);
        summary.put("avgMemoryLastHour", avgMemory != null ? avgMemory : 0);

        return summary;
    }

    @Override
    public List<AlertDto> getActiveAlerts() {
        List<AlertDto> alerts = new ArrayList<>();

        try {
            // Check CPU usage
            OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
            double cpuUsage = osBean.getProcessCpuLoad() * 100;
            if (cpuUsage > 80) {
                alerts.add(AlertDto.builder()
                        .id(UUID.randomUUID().toString())
                        .type(AlertDto.AlertType.CPU_HIGH)
                        .severity(AlertDto.AlertSeverity.WARNING)
                        .title("High CPU Usage")
                        .message(String.format("CPU usage is %.1f%%", cpuUsage))
                        .timestamp(LocalDateTime.now())
                        .build());
            }

            // Check memory usage
            Runtime runtime = Runtime.getRuntime();
            double memoryUsage = (runtime.totalMemory() - runtime.freeMemory()) * 100.0 / runtime.totalMemory();
            if (memoryUsage > 85) {
                alerts.add(AlertDto.builder()
                        .id(UUID.randomUUID().toString())
                        .type(AlertDto.AlertType.MEMORY_HIGH)
                        .severity(AlertDto.AlertSeverity.WARNING)
                        .title("High Memory Usage")
                        .message(String.format("Memory usage is %.1f%%", memoryUsage))
                        .timestamp(LocalDateTime.now())
                        .build());
            }

            // Check disk space
            File root = new File("/");
            double diskUsage = (root.getTotalSpace() - root.getFreeSpace()) * 100.0 / root.getTotalSpace();
            if (diskUsage > 90) {
                alerts.add(AlertDto.builder()
                        .id(UUID.randomUUID().toString())
                        .type(AlertDto.AlertType.DISK_SPACE_LOW)
                        .severity(AlertDto.AlertSeverity.ERROR)
                        .title("Low Disk Space")
                        .message(String.format("Disk usage is %.1f%%", diskUsage))
                        .timestamp(LocalDateTime.now())
                        .build());
            }

        } catch (Exception e) {
            log.error("Error checking alerts", e);
        }

        return alerts;
    }

    @Override
    public void addMetricSubscriber(SseEmitter emitter) {
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        // Send initial data
        try {
            Map<String, Object> initialData = getMetricsSummary();
            emitter.send(SseEmitter.event()
                    .name("metrics")
                    .data(initialData));
        } catch (IOException e) {
            emitters.remove(emitter);
        }
    }

    @Override
    public void removeMetricSubscriber(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    // Broadcast metrics to all subscribers
    @Scheduled(fixedDelay = 5000) // Every 5 seconds
    public void broadcastMetrics() {
        if (emitters.isEmpty()) {
            return;
        }

        Map<String, Object> metrics = getMetricsSummary();
        metrics.put("timestamp", LocalDateTime.now());

        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("metrics")
                        .data(metrics));
            } catch (IOException e) {
                // Client disconnected, remove from list
                log.debug("SSE client disconnected: {}", e.getMessage());
                deadEmitters.add(emitter);
            } catch (Exception e) {
                // Any other error, remove from list
                log.warn("Error sending SSE event: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        });

        // Clean up dead emitters
        deadEmitters.forEach(emitter -> {
            try {
                emitter.complete();
            } catch (Exception ignored) {
                // Ignore completion errors
            }
        });
        emitters.removeAll(deadEmitters);
    }

    // Очистка старых метрик (запускается каждый день в 2 ночи)
    @Override
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupOldMetrics() {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

            int deletedSystemMetrics = systemMetricRepository.deleteByTimestampBefore(thirtyDaysAgo);
            int deletedHealthChecks = healthCheckHistoryRepository.deleteByTimestampBefore(thirtyDaysAgo);
            int deletedApiMetrics = apiMetricRepository.deleteByTimestampBefore(thirtyDaysAgo);

            LocalDate ninetyDaysAgo = LocalDate.now().minusDays(90);
            int deletedBusinessMetrics = businessMetricRepository.deleteByMetricDateBefore(ninetyDaysAgo);

            log.info("Cleanup completed: deleted {} system metrics, {} health checks, {} API metrics, {} business metrics",
                    deletedSystemMetrics, deletedHealthChecks, deletedApiMetrics, deletedBusinessMetrics);

        } catch (Exception e) {
            log.error("Error during metrics cleanup", e);
        }
    }

    private SystemMetric createMetric(LocalDateTime timestamp, String type, String name,
                                      Number value, String unit) {
        return SystemMetric.builder()
                .timestamp(timestamp)
                .metricType(type)
                .metricName(name)
                .metricValue(BigDecimal.valueOf(value.doubleValue()))
                .unit(unit)
                .build();
    }

    private MetricDto toMetricDto(SystemMetric metric) {
        return MetricDto.builder()
                .timestamp(metric.getTimestamp())
                .name(metric.getMetricName())
                .value(metric.getMetricValue().doubleValue())
                .unit(metric.getUnit())
                .build();
    }

    private Map<String, Long> convertCountryStats(List<Object[]> stats) {
        return stats.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0],
                        arr -> (Long) arr[1]
                ));
    }
}