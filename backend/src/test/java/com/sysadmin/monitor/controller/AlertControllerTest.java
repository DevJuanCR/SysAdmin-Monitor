package com.sysadmin.monitor.controller;

import com.sysadmin.monitor.dto.AlertDTO;
import com.sysadmin.monitor.service.MetricService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricService metricService;

    @Test
    void getAlerts_shouldReturn200() throws Exception {
        AlertDTO alert = AlertDTO.builder()
                .hostname("PC-01")
                .metric("cpu")
                .value(92.5)
                .threshold(85.0)
                .build();

        when(metricService.getAlerts()).thenReturn(List.of(alert));

        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hostname").value("PC-01"))
                .andExpect(jsonPath("$[0].metric").value("cpu"))
                .andExpect(jsonPath("$[0].value").value(92.5))
                .andExpect(jsonPath("$[0].threshold").value(85.0));
    }

    @Test
    void getAlerts_withNoAlerts_shouldReturnEmptyList() throws Exception {
        when(metricService.getAlerts()).thenReturn(List.of());

        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
