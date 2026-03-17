package com.chp.medical.controller;

import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 医生工作量统计
 */
@RestController
@RequestMapping("/medical/stats")
@RequiredArgsConstructor
public class StaffStatsController {

    private final JdbcTemplate jdbcTemplate;

    /** 个人工作量统计 */
    @GetMapping("/my")
    public Result<Map<String, Object>> myStats(@RequestAttribute("staffId") Long staffId) {
        // 本月接诊量
        Long visitCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chp_resident.visit_record " +
                "WHERE staff_id = ? AND MONTH(visit_date) = MONTH(NOW()) AND YEAR(visit_date) = YEAR(NOW())",
                Long.class, staffId);

        // 本月处方量
        Long prescCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM chp_resident.prescription " +
                "WHERE doctor_id = ? AND MONTH(created_at) = MONTH(NOW()) AND YEAR(created_at) = YEAR(NOW())",
                Long.class, staffId);

        // 近7天每日接诊量趋势
        List<Map<String, Object>> trend = jdbcTemplate.queryForList(
                "SELECT DATE(visit_date) AS date, COUNT(*) AS count " +
                "FROM chp_resident.visit_record " +
                "WHERE staff_id = ? AND visit_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                "GROUP BY DATE(visit_date) ORDER BY date", staffId);

        return Result.success(Map.of(
                "visitCount", visitCount != null ? visitCount : 0,
                "prescCount", prescCount != null ? prescCount : 0,
                "trend", trend));
    }
}
