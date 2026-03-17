package com.chp.controller;

import com.chp.admin.controller.ReportController;
import com.chp.admin.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.nullable;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReportController 集成测试（MockMvc 独立模式）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController 集成测试")
class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    @Test
    @DisplayName("GET /admin/report/overview 成功")
    void overview_success() throws Exception {
        Map<String, Object> overview = new HashMap<>();
        overview.put("drugAlertCount", 3);
        overview.put("totalDrugs", 120);
        when(reportService.overview()).thenReturn(overview);

        mockMvc.perform(get("/admin/report/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.drugAlertCount").value(3))
                .andExpect(jsonPath("$.data.totalDrugs").value(120));
    }

    @Test
    @DisplayName("GET /admin/report/drug 成功")
    void drugReport_success() throws Exception {
        Map<String, Object> report = new HashMap<>();
        report.put("topConsumed", List.of(Map.of("drugName", "阿莫西林", "quantity", 100)));
        when(reportService.drugReport(any(LocalDate.class), any(LocalDate.class))).thenReturn(report);

        mockMvc.perform(get("/admin/report/drug")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-03-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.topConsumed").isArray());
    }

    @Test
    @DisplayName("GET /admin/report/visit-trend 成功")
    void visitTrend_success() throws Exception {
        Map<String, Object> trend = new HashMap<>();
        trend.put("dates", List.of("2026-03-10", "2026-03-11"));
        trend.put("counts", List.of(10, 15));
        when(reportService.visitTrend7Days()).thenReturn(trend);

        mockMvc.perform(get("/admin/report/visit-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dates").isArray())
                .andExpect(jsonPath("$.data.counts").isArray());
    }

    @Test
    @DisplayName("GET /admin/report/dept-load 成功")
    void deptLoad_success() throws Exception {
        Map<String, Object> load = new HashMap<>();
        load.put("deptData", List.of(Map.of("name", "全科门诊", "value", 50)));
        when(reportService.deptLoadDistribution()).thenReturn(load);

        mockMvc.perform(get("/admin/report/dept-load"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.deptData").isArray());
    }

    @Test
    @DisplayName("GET /admin/report/appointment 成功")
    void appointmentTrend_success() throws Exception {
        Map<String, Object> trend = new HashMap<>();
        trend.put("dates", List.of("2026-03-10"));
        trend.put("counts", List.of(8));
        when(reportService.appointmentTrend(any(LocalDate.class), any(LocalDate.class), nullable(String.class)))
                .thenReturn(trend);

        mockMvc.perform(get("/admin/report/appointment")
                        .param("startDate", "2026-03-10")
                        .param("endDate", "2026-03-16"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dates").isArray());
    }

    @Test
    @DisplayName("POST /admin/report/export 返回 Excel 字节流")
    void exportOverview_success() throws Exception {
        byte[] mockExcel = new byte[]{0x50, 0x4B, 0x03, 0x04}; // 模拟 xlsx 文件头
        when(reportService.exportOverview()).thenReturn(mockExcel);

        mockMvc.perform(post("/admin/report/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=report_overview.xlsx"));
    }
}
