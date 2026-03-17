package com.chp.resident.controller;

import com.chp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 居民端随访互动
 */
@RestController
@RequestMapping("/resident/follow-up")
@RequiredArgsConstructor
public class ResidentFollowUpController {

    private final JdbcTemplate jdbcTemplate;

    /** 我的随访计划 */
    @GetMapping("/plan")
    public Result<List<Map<String, Object>>> myPlans(@RequestAttribute("residentId") Long residentId) {
        List<Map<String, Object>> plans = jdbcTemplate.queryForList(
                "SELECT fp.id, fp.resident_id, fp.disease_type AS diseaseType, " +
                "fp.frequency, fp.next_follow_date AS nextFollowDate, fp.status, " +
                "fp.created_at AS createdAt " +
                "FROM chp_admin.follow_up_plan fp " +
                "WHERE fp.resident_id = ? AND fp.status = 1 " +
                "ORDER BY fp.next_follow_date ASC", residentId);
        return Result.success(plans);
    }

    /** 随访记录 */
    @GetMapping("/record/{planId}")
    public Result<List<Map<String, Object>>> records(@PathVariable Long planId) {
        List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT id, follow_date AS followDate, blood_pressure AS bloodPressure, " +
                "blood_glucose AS bloodGlucose, weight, note, operator_name AS operatorName " +
                "FROM chp_admin.follow_up_record " +
                "WHERE plan_id = ? ORDER BY follow_date DESC", planId);
        return Result.success(records);
    }

    /** 居民自报指标 */
    @PostMapping("/self-report")
    public Result<?> selfReport(@RequestBody Map<String, Object> body,
                                @RequestAttribute("residentId") Long residentId) {
        Long planId = Long.valueOf(body.get("planId").toString());
        String bp = (String) body.getOrDefault("bloodPressure", "");
        String bg = (String) body.getOrDefault("bloodGlucose", "");
        String wt = (String) body.getOrDefault("weight", "");
        String note = (String) body.getOrDefault("note", "居民自报");

        jdbcTemplate.update(
                "INSERT INTO chp_admin.follow_up_record " +
                "(plan_id, follow_date, blood_pressure, blood_glucose, weight, note, operator_name) " +
                "VALUES (?, NOW(), ?, ?, ?, ?, '居民自报')",
                planId, bp, bg, wt, note);
        return Result.success();
    }
}
