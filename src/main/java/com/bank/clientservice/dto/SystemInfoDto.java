package com.bank.clientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemInfoDto {
    private String hostname;
    private String ipAddress;
    private String osName;
    private String osVersion;
    private String osArchitecture;
    private Integer availableProcessors;
    private String javaVersion;
    private String javaVendor;
    private String javaHome;
    private LocalDateTime startTime;
    private Long uptimeMillis;
    private Map<String, Object> systemProperties;
    private Map<String, Object> environmentVariables;
    private DiskSpaceInfo diskSpace;
    private NetworkInfo network;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiskSpaceInfo {
        private Long totalSpace;
        private Long freeSpace;
        private Long usableSpace;
        private Double usagePercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NetworkInfo {
        private String primaryInterface;
        private List<String> ipAddresses;
        private String macAddress;
    }
}