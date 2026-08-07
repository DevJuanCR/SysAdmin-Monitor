package com.sysadmin.monitor.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemMetricDTO {

    @NotBlank
    private String hostname;

    @NotNull
    @Min(0)
    @Max(100)
    private Double cpuUsage;

    @NotNull
    @Min(0)
    @Max(100)
    private Double ramUsage;

    @NotNull
    @Min(0)
    @Max(100)
    private Double diskUsage;
}