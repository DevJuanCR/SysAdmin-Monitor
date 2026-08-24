package com.sysadmin.monitor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sysadmin.monitor.dto.MetricSummaryDTO;
import com.sysadmin.monitor.dto.SystemMetricDTO;
import com.sysadmin.monitor.entity.SystemMetric;
import com.sysadmin.monitor.service.MetricService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MetricController.class)
class MetricControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricService metricService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postMetric_withValidData_shouldReturn201() throws Exception {
        SystemMetricDTO dto = new SystemMetricDTO("PC-01", 45.2, 67.8, 71.5, 1024.0, 2048.0);

        SystemMetric saved = SystemMetric.builder()
                .id(1L)
                .hostname("PC-01")
                .timestamp(LocalDateTime.now())
                .cpuUsage(45.2)
                .ramUsage(67.8)
                .diskUsage(71.5)
                .build();

        when(metricService.saveMetric(any(SystemMetricDTO.class))).thenReturn(saved);

        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.hostname").value("PC-01"))
                .andExpect(jsonPath("$.cpuUsage").value(45.2));
    }

    @Test
    void postMetric_withNullHostname_shouldReturn400() throws Exception {
        String json = "{\"cpuUsage\": 45.2, \"ramUsage\": 67.8, \"diskUsage\": 71.5, \"netSent\": 1024.0, \"netRecv\": 2048.0}";

        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.hostname").exists());
    }

    @Test
    void postMetric_withNullCpu_shouldReturn400() throws Exception {
        String json = "{\"hostname\": \"PC-01\", \"ramUsage\": 67.8, \"diskUsage\": 71.5, \"netSent\": 1024.0, \"netRecv\": 2048.0}";

        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cpuUsage").exists());
    }

    @Test
    void postMetric_withCpuOver100_shouldReturn400() throws Exception {
        String json = "{\"hostname\": \"PC-01\", \"cpuUsage\": 150.0, \"ramUsage\": 67.8, \"diskUsage\": 71.5, \"netSent\": 1024.0, \"netRecv\": 2048.0}";

        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cpuUsage").exists());
    }

    @Test
    void postMetric_withNegativeRam_shouldReturn400() throws Exception {
        String json = "{\"hostname\": \"PC-01\", \"cpuUsage\": 45.2, \"ramUsage\": -5.0, \"diskUsage\": 71.5, \"netSent\": 1024.0, \"netRecv\": 2048.0}";

        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ramUsage").exists());
    }

    @Test
    void getMetrics_withoutHostname_shouldReturn200() throws Exception {
        SystemMetric metric = SystemMetric.builder()
                .id(1L)
                .hostname("PC-01")
                .timestamp(LocalDateTime.now())
                .cpuUsage(45.2)
                .ramUsage(67.8)
                .diskUsage(71.5)
                .build();

        when(metricService.getLatestMetrics(null, 20)).thenReturn(List.of(metric));

        mockMvc.perform(get("/api/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname").value("PC-01"));
    }

    @Test
    void getMetrics_withHostname_shouldReturn200() throws Exception {
        SystemMetric metric = SystemMetric.builder()
                .id(1L)
                .hostname("PC-01")
                .timestamp(LocalDateTime.now())
                .cpuUsage(45.2)
                .ramUsage(67.8)
                .diskUsage(71.5)
                .build();

        when(metricService.getLatestMetrics(eq("PC-01"), eq(20))).thenReturn(List.of(metric));

        mockMvc.perform(get("/api/metrics").param("hostname", "PC-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname").value("PC-01"));
    }

    @Test
    void getMetrics_withNoData_shouldReturnEmptyList() throws Exception {
        when(metricService.getLatestMetrics(null, 20)).thenReturn(List.of());

        mockMvc.perform(get("/api/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getHosts_shouldReturn200() throws Exception {
        when(metricService.getHostnames()).thenReturn(List.of("PC-01", "PC-02"));

        mockMvc.perform(get("/api/metrics/hosts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("PC-01"))
                .andExpect(jsonPath("$[1]").value("PC-02"));
    }

    @Test
    void postMetric_withNullDisk_shouldReturn400() throws Exception {
        String json = "{\"hostname\": \"PC-01\", \"cpuUsage\": 45.2, \"ramUsage\": 67.8, \"netSent\": 1024.0, \"netRecv\": 2048.0}";

        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.diskUsage").exists());
    }

    @Test
    void postMetric_withDiskOver100_shouldReturn400() throws Exception {
        String json = "{\"hostname\": \"PC-01\", \"cpuUsage\": 45.2, \"ramUsage\": 67.8, \"diskUsage\": 120.0, \"netSent\": 1024.0, \"netRecv\": 2048.0}";

        mockMvc.perform(post("/api/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.diskUsage").exists());
    }

    @Test
    void getMetrics_shouldIncludeDiskUsage() throws Exception {
        SystemMetric metric = SystemMetric.builder()
                .id(1L)
                .hostname("PC-01")
                .timestamp(LocalDateTime.now())
                .cpuUsage(45.2)
                .ramUsage(67.8)
                .diskUsage(88.4)
                .build();

        when(metricService.getLatestMetrics(null, 20)).thenReturn(List.of(metric));

        mockMvc.perform(get("/api/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].diskUsage").value(88.4));
    }

    @Test
    void getSummary_shouldReturn200() throws Exception {
        MetricSummaryDTO summary = MetricSummaryDTO.builder()
                .hostname("PC-01")
                .cpuAvg(40.0)
                .cpuMax(80.0)
                .ramAvg(50.0)
                .ramMax(70.0)
                .diskAvg(60.0)
                .diskMax(90.0)
                .build();

        when(metricService.getSummary()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/metrics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname").value("PC-01"))
                .andExpect(jsonPath("$[0].cpuAvg").value(40.0))
                .andExpect(jsonPath("$[0].diskMax").value(90.0));
    }

    @Test
    void getSummary_withNoData_shouldReturnEmptyList() throws Exception {
        when(metricService.getSummary()).thenReturn(List.of());

        mockMvc.perform(get("/api/metrics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMetrics_withLimit_shouldPassLimitToService() throws Exception {
        SystemMetric metric = SystemMetric.builder()
                .id(1L)
                .hostname("PC-01")
                .timestamp(LocalDateTime.now())
                .cpuUsage(45.2)
                .ramUsage(67.8)
                .diskUsage(71.5)
                .build();

        when(metricService.getLatestMetrics(null, 5)).thenReturn(List.of(metric));

        mockMvc.perform(get("/api/metrics").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname").value("PC-01"));
    }
}
