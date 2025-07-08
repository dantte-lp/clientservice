package com.bank.clientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiEndpointStatsDto {
    private String endpoint;
    private String method;
    private Long totalCalls;
    private Long successfulCalls;
    private Long failedCalls;
    private Double averageResponseTime;
    private Double maxResponseTime;
    private Double minResponseTime;
    private Map<Integer, Long> statusCodeDistribution;
}