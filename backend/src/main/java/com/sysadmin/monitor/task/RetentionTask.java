package com.sysadmin.monitor.task;

import com.sysadmin.monitor.service.MetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RetentionTask {

    private final MetricService metricService;

    @Scheduled(cron = "${monitor.retention.cron:0 0 4 * * *}")
    public void limpiarMetricasAntiguas() {
        metricService.deleteOldMetrics();
    }
}
