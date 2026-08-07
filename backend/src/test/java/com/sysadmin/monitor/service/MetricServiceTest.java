package com.sysadmin.monitor.service;

import com.sysadmin.monitor.dto.SystemMetricDTO;
import com.sysadmin.monitor.entity.SystemMetric;
import com.sysadmin.monitor.repository.SystemMetricRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricServiceTest {

    @Mock
    private SystemMetricRepository metricRepository;

    @InjectMocks
    private MetricService metricService;

    @Test
    void saveMetric_shouldSaveAndReturnMetric() {
        SystemMetricDTO dto = new SystemMetricDTO("PC-01", 45.2, 67.8, 71.5);

        SystemMetric saved = SystemMetric.builder()
                .id(1L)
                .hostname("PC-01")
                .timestamp(LocalDateTime.now())
                .cpuUsage(45.2)
                .ramUsage(67.8)
                .diskUsage(71.5)
                .build();

        when(metricRepository.save(any(SystemMetric.class))).thenReturn(saved);

        SystemMetric result = metricService.saveMetric(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("PC-01", result.getHostname());
        assertEquals(45.2, result.getCpuUsage());
        assertEquals(67.8, result.getRamUsage());
        assertEquals(71.5, result.getDiskUsage());
        verify(metricRepository, times(1)).save(any(SystemMetric.class));
    }

    @Test
    void getLatestMetrics_withHostname_shouldFilterByHost() {
        SystemMetric m1 = SystemMetric.builder()
                .id(1L)
                .hostname("PC-01")
                .timestamp(LocalDateTime.now())
                .cpuUsage(30.0)
                .ramUsage(50.0)
                .build();

        when(metricRepository.findTop20ByHostnameOrderByTimestampDesc("PC-01"))
                .thenReturn(List.of(m1));

        List<SystemMetric> result = metricService.getLatestMetrics("PC-01");

        assertEquals(1, result.size());
        assertEquals("PC-01", result.get(0).getHostname());
    }

    @Test
    void getLatestMetrics_withoutHostname_shouldReturnAll() {
        SystemMetric m1 = SystemMetric.builder()
                .id(2L)
                .hostname("PC-01")
                .timestamp(LocalDateTime.now().minusSeconds(10))
                .cpuUsage(30.0)
                .ramUsage(50.0)
                .build();

        SystemMetric m2 = SystemMetric.builder()
                .id(1L)
                .hostname("PC-02")
                .timestamp(LocalDateTime.now().minusSeconds(20))
                .cpuUsage(20.0)
                .ramUsage(40.0)
                .build();

        when(metricRepository.findTop20ByOrderByTimestampDesc()).thenReturn(List.of(m1, m2));

        List<SystemMetric> result = metricService.getLatestMetrics(null);

        assertEquals(2, result.size());
    }

    @Test
    void getLatestMetrics_shouldReturnEmptyList() {
        when(metricRepository.findTop20ByOrderByTimestampDesc()).thenReturn(List.of());

        List<SystemMetric> result = metricService.getLatestMetrics(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getHostnames_shouldReturnDistinctHosts() {
        when(metricRepository.findDistinctHostnames()).thenReturn(List.of("PC-01", "PC-02"));

        List<String> result = metricService.getHostnames();

        assertEquals(2, result.size());
        assertTrue(result.contains("PC-01"));
        assertTrue(result.contains("PC-02"));
    }
}