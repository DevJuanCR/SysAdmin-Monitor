package com.sysadmin.monitor.controller;

import com.sysadmin.monitor.dto.MetricSummaryDTO;
import com.sysadmin.monitor.dto.SystemMetricDTO;
import com.sysadmin.monitor.entity.SystemMetric;
import com.sysadmin.monitor.service.MetricService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@CrossOrigin("*")
@RequiredArgsConstructor
public class MetricController {

    private final MetricService metricService;

    @PostMapping
    public ResponseEntity<SystemMetric> receiveMetric(@Valid @RequestBody SystemMetricDTO metricDTO) {
        SystemMetric savedMetric = metricService.saveMetric(metricDTO);
        return new ResponseEntity<>(savedMetric, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<SystemMetric>> getLatestMetrics(
            @RequestParam(required = false) String hostname,
            @RequestParam(defaultValue = "20") int limit) {
        List<SystemMetric> metrics = metricService.getLatestMetrics(hostname, limit);
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/summary")
    public ResponseEntity<List<MetricSummaryDTO>> getSummary() {
        List<MetricSummaryDTO> summary = metricService.getSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/hosts")
    public ResponseEntity<List<String>> getHostnames() {
        List<String> hostnames = metricService.getHostnames();
        return ResponseEntity.ok(hostnames);
    }
}