package com.chp.admin.controller;

import com.chp.admin.service.ReportService;
import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** GET /api/admin/report/overview */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(reportService.overview());
    }

    /** GET /api/admin/report/drug?startDate=2026-01-01&endDate=2026-03-15 */
    @GetMapping("/drug")
    public Result<Map<String, Object>> drugReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return Result.success(reportService.drugReport(startDate, endDate));
    }

    /** GET /api/admin/report/visit-trend — 近7天预约量趋势 */
    @GetMapping("/visit-trend")
    public Result<Map<String, Object>> visitTrend() {
        return Result.success(reportService.visitTrend7Days());
    }

    /** GET /api/admin/report/dept-load — 科室预约分布 */
    @GetMapping("/dept-load")
    public Result<Map<String, Object>> deptLoad() {
        return Result.success(reportService.deptLoadDistribution());
    }

    /** GET /api/admin/report/public-health — 公卫服务统计 */
    @GetMapping("/public-health")
    public Result<Map<String, Object>> publicHealth() {
        return Result.success(reportService.publicHealthReport());
    }
    /** GET /api/admin/report/vaccine — 接种统计 */
    @GetMapping("/vaccine")
    public Result<Map<String, Object>> vaccine() {
        return Result.success(reportService.vaccineReport());
    }

    /** GET /api/admin/report/follow-up — 随访完成率统计 */
    @GetMapping("/follow-up")
    public Result<Map<String, Object>> followUp() {
        return Result.success(reportService.followUpReport());
    }

    /** GET /api/admin/report/contract — 签约统计 */
    @GetMapping("/contract")
    public Result<Map<String, Object>> contract() {
        return Result.success(reportService.contractReport());
    }

    /** GET /api/admin/report/appointment?startDate=&endDate=&deptCode= — 预约趋势 */
    @GetMapping("/appointment")
    public Result<Map<String, Object>> appointmentTrend(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String deptCode) {
        return Result.success(reportService.appointmentTrend(startDate, endDate, deptCode));
    }

    /** POST /api/admin/report/export — 导出概览 Excel */
    @PostMapping("/export")
    public org.springframework.http.ResponseEntity<byte[]> exportOverview() {
        byte[] excelBytes = reportService.exportOverview();
        return org.springframework.http.ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report_overview.xlsx")
                .contentType(org.springframework.http.MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
    /** GET /api/admin/report/doctor-workload — 医生本周工作量排行 */
    @GetMapping("/doctor-workload")
    public Result<?> doctorWorkload() {
        return Result.success(reportService.doctorWorkloadRank());
    }

}
