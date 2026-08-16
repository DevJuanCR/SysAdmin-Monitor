package com.sysadmin.monitor.service;

import com.sysadmin.monitor.dto.SystemMetricDTO;
import com.sysadmin.monitor.entity.SystemMetric;
import com.sysadmin.monitor.repository.SystemMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricService {

    private final SystemMetricRepository metricRepository;

    public SystemMetric saveMetric(SystemMetricDTO dto) {

        SystemMetric metric = SystemMetric.builder()
                .hostname(dto.getHostname())
                .timestamp(LocalDateTime.now())
                .cpuUsage(dto.getCpuUsage())
                .ramUsage(dto.getRamUsage())
                .diskUsage(dto.getDiskUsage())
                .netSent(dto.getNetSent())
                .netRecv(dto.getNetRecv())
                .build();

        SystemMetric savedMetric = metricRepository.save(metric);

        log.info("Metrica guardada - Host: {} ID: {} CPU: {}% RAM: {}% Disco: {}% Red: {}/{} B/s",
                savedMetric.getHostname(),
                savedMetric.getId(),
                savedMetric.getCpuUsage(),
                savedMetric.getRamUsage(),
                savedMetric.getDiskUsage(),
                savedMetric.getNetSent(),
                savedMetric.getNetRecv());

        return savedMetric;
    }

    public List<SystemMetric> getLatestMetrics(String hostname) {
        List<SystemMetric> metrics;

        if (hostname != null && !hostname.isBlank()) {
            metrics = metricRepository.findTop20ByHostnameOrderByTimestampDesc(hostname);
        } else {
            metrics = metricRepository.findTop20ByOrderByTimestampDesc();
        }

        return metrics.reversed();
    }

    public List<String> getHostnames() {
        return metricRepository.findDistinctHostnames();
    }
}