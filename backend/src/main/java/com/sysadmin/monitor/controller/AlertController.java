package com.sysadmin.monitor.controller;

import com.sysadmin.monitor.dto.AlertDTO;
import com.sysadmin.monitor.service.MetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AlertController {

    private final MetricService metricService;

    @GetMapping
    public ResponseEntity<List<AlertDTO>> getAlerts() {
        List<AlertDTO> alerts = metricService.getAlerts();
        return ResponseEntity.ok(alerts);
    }
}
