package com.sysadmin.monitor.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricSummaryDTO {

    private String hostname;

    private Double cpuAvg;

    private Double cpuMax;

    private Double ramAvg;

    private Double ramMax;

    private Double diskAvg;

    private Double diskMax;
}
