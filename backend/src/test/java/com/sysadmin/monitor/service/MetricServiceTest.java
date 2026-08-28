package com.sysadmin.monitor.service;

import com.sysadmin.monitor.dto.AlertDTO;
import com.sysadmin.monitor.dto.MetricSummaryDTO;
import com.sysadmin.monitor.dto.SystemMetricDTO;
import com.sysadmin.monitor.entity.SystemMetric;
import com.sysadmin.monitor.repository.SystemMetricRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricServiceTest {

    @Mock
    private SystemMetricRepository metricRepository;

    @InjectMocks
    private MetricService metricService;

    @Test
    void saveMetric_shouldSaveAndReturnMetric() {
        SystemMetricDTO dto = new SystemMetricDTO("PC-01", 45.2, 67.8, 71.5, 1024.0, 2048.0);

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

        when(metricRepository.findByHostnameOrderByTimestampDesc(eq("PC-01"), any(Pageable.class)))
                .thenReturn(List.of(m1));

        List<SystemMetric> result = metricService.getLatestMetrics("PC-01", 20);

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

        when(metricRepository.findByOrderByTimestampDesc(any(Pageable.class))).thenReturn(List.of(m1, m2));

        List<SystemMetric> result = metricService.getLatestMetrics(null, 20);

        assertEquals(2, result.size());
    }

    @Test
    void getLatestMetrics_shouldReturnEmptyList() {
        when(metricRepository.findByOrderByTimestampDesc(any(Pageable.class))).thenReturn(List.of());

        List<SystemMetric> result = metricService.getLatestMetrics(null, 20);

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

    @Test
    void saveMetric_shouldKeepDiskUsage() {
        SystemMetricDTO dto = new SystemMetricDTO("PC-01", 45.2, 67.8, 88.4, 1024.0, 2048.0);

        SystemMetric saved = SystemMetric.builder()
                .id(1L)
                .hostname("PC-01")
                .timestamp(LocalDateTime.now())
                .cpuUsage(45.2)
                .ramUsage(67.8)
                .diskUsage(88.4)
                .build();

        when(metricRepository.save(any(SystemMetric.class))).thenReturn(saved);

        metricService.saveMetric(dto);

        ArgumentCaptor<SystemMetric> captor = ArgumentCaptor.forClass(SystemMetric.class);
        verify(metricRepository).save(captor.capture());
        assertEquals(88.4, captor.getValue().getDiskUsage());
    }

    @Test
    void getLatestMetrics_shouldReturnDiskUsage() {
        SystemMetric m1 = SystemMetric.builder()
                .id(1L)
                .hostname("PC-01")
                .timestamp(LocalDateTime.now())
                .cpuUsage(30.0)
                .ramUsage(50.0)
                .diskUsage(72.3)
                .build();

        when(metricRepository.findByHostnameOrderByTimestampDesc(eq("PC-01"), any(Pageable.class)))
                .thenReturn(List.of(m1));

        List<SystemMetric> result = metricService.getLatestMetrics("PC-01", 20);

        assertEquals(72.3, result.get(0).getDiskUsage());
    }

    @Test
    void getSummary_shouldReturnOneRowPerHost() {
        MetricSummaryDTO s1 = MetricSummaryDTO.builder()
                .hostname("PC-01")
                .cpuAvg(40.0)
                .cpuMax(80.0)
                .ramAvg(50.0)
                .ramMax(70.0)
                .diskAvg(60.0)
                .diskMax(90.0)
                .build();

        MetricSummaryDTO s2 = MetricSummaryDTO.builder()
                .hostname("PC-02")
                .cpuAvg(20.0)
                .cpuMax(35.0)
                .ramAvg(30.0)
                .ramMax(45.0)
                .diskAvg(55.0)
                .diskMax(65.0)
                .build();

        when(metricRepository.findSummaryByHost()).thenReturn(List.of(s1, s2));

        List<MetricSummaryDTO> result = metricService.getSummary();

        assertEquals(2, result.size());
        assertEquals("PC-01", result.get(0).getHostname());
        assertEquals(80.0, result.get(0).getCpuMax());
        assertEquals(55.0, result.get(1).getDiskAvg());
    }

    @Test
    void getSummary_withNoData_shouldReturnEmptyList() {
        when(metricRepository.findSummaryByHost()).thenReturn(List.of());

        List<MetricSummaryDTO> result = metricService.getSummary();

        assertTrue(result.isEmpty());
        verify(metricRepository, times(1)).findSummaryByHost();
    }

    @Test
    void getLatestMetrics_withLimitTooBig_shouldUseMaximum() {
        when(metricRepository.findByOrderByTimestampDesc(any(Pageable.class))).thenReturn(List.of());

        metricService.getLatestMetrics(null, 500);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(metricRepository).findByOrderByTimestampDesc(captor.capture());
        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    void getLatestMetrics_withLimitZero_shouldUseOne() {
        when(metricRepository.findByOrderByTimestampDesc(any(Pageable.class))).thenReturn(List.of());

        metricService.getLatestMetrics(null, 0);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(metricRepository).findByOrderByTimestampDesc(captor.capture());
        assertEquals(1, captor.getValue().getPageSize());
    }

    @Test
    void getAlerts_withCpuOverThreshold_shouldReturnAlert() {
        ReflectionTestUtils.setField(metricService, "cpuThreshold", 85.0);
        ReflectionTestUtils.setField(metricService, "ramThreshold", 90.0);

        SystemMetric m1 = SystemMetric.builder()
                .id(1L)
                .hostname("PC-01")
                .timestamp(LocalDateTime.now())
                .cpuUsage(92.5)
                .ramUsage(60.0)
                .diskUsage(50.0)
                .build();

        when(metricRepository.findDistinctHostnames()).thenReturn(List.of("PC-01"));
        when(metricRepository.findByHostnameOrderByTimestampDesc(eq("PC-01"), any(Pageable.class)))
                .thenReturn(List.of(m1));

        List<AlertDTO> result = metricService.getAlerts();

        assertEquals(1, result.size());
        assertEquals("cpu", result.get(0).getMetric());
        assertEquals(92.5, result.get(0).getValue());
        assertEquals(85.0, result.get(0).getThreshold());
    }

    @Test
    void getAlerts_withCpuAndRamOverThreshold_shouldReturnTwoAlerts() {
        ReflectionTestUtils.setField(metricService, "cpuThreshold", 85.0);
        ReflectionTestUtils.setField(metricService, "ramThreshold", 90.0);

        SystemMetric m1 = SystemMetric.builder()
                .id(1L)
                .hostname("PC-02")
                .timestamp(LocalDateTime.now())
                .cpuUsage(99.0)
                .ramUsage(95.0)
                .diskUsage(50.0)
                .build();

        when(metricRepository.findDistinctHostnames()).thenReturn(List.of("PC-02"));
        when(metricRepository.findByHostnameOrderByTimestampDesc(eq("PC-02"), any(Pageable.class)))
                .thenReturn(List.of(m1));

        List<AlertDTO> result = metricService.getAlerts();

        assertEquals(2, result.size());
        assertEquals("cpu", result.get(0).getMetric());
        assertEquals("ram", result.get(1).getMetric());
        assertEquals("PC-02", result.get(1).getHostname());
    }

    @Test
    void getAlerts_underThreshold_shouldReturnEmptyList() {
        ReflectionTestUtils.setField(metricService, "cpuThreshold", 85.0);
        ReflectionTestUtils.setField(metricService, "ramThreshold", 90.0);

        SystemMetric m1 = SystemMetric.builder()
                .id(1L)
                .hostname("PC-01")
                .timestamp(LocalDateTime.now())
                .cpuUsage(30.0)
                .ramUsage(40.0)
                .diskUsage(50.0)
                .build();

        when(metricRepository.findDistinctHostnames()).thenReturn(List.of("PC-01"));
        when(metricRepository.findByHostnameOrderByTimestampDesc(eq("PC-01"), any(Pageable.class)))
                .thenReturn(List.of(m1));

        List<AlertDTO> result = metricService.getAlerts();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAlerts_withoutHosts_shouldReturnEmptyList() {
        when(metricRepository.findDistinctHostnames()).thenReturn(List.of());

        List<AlertDTO> result = metricService.getAlerts();

        assertTrue(result.isEmpty());
        verify(metricRepository, never()).findByHostnameOrderByTimestampDesc(anyString(), any(Pageable.class));
    }
}
