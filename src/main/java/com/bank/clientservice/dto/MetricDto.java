package com.bank.clientservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricDto {
    private LocalDateTime timestamp;
    private String name;
    private Double value;
    private String unit;
    private String type;
}
