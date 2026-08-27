package com.sysadmin.monitor.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDTO {

    private String hostname;

    private String metric;

    private Double value;

    private Double threshold;
}
