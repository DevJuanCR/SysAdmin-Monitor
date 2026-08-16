package com.sysadmin.monitor.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String hostname;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Double cpuUsage;

    @Column(nullable = false)
    private Double ramUsage;

    @Column
    private Double diskUsage;

    @Column
    private Double netSent;

    @Column
    private Double netRecv;
}