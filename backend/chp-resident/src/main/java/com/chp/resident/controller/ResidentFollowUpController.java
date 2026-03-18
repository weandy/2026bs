package com.chp.resident.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.admin.entity.FamilyDoctorContract;
import com.chp.admin.mapper.FamilyDoctorContractMapper;
import com.chp.common.result.Result;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 居民端 — 签约与随访
 */
@RestController
@RequestMapping("/resident/follow-up")
@RequiredArgsConstructor
public class ResidentFollowUpController {

    private final JdbcTemplate jdbcTemplate;
    private final FamilyDoctorContractMapper contractMapper;

    // ===== 签约相关 =====

    /** 查看我的签约状态（最新一条） */
    @GetMapping("/contract")
    public Result<?> myContract() {
        Long residentId = SecurityUtils.getCurrentUserId();
        FamilyDoctorContract c = contractMapper.selectOne(
                new LambdaQueryWrapper<FamilyDoctorContract>()
                        .eq(FamilyDoctorContract::getResidentId, residentId)
                        .in(FamilyDoctorContract::getStatus, "PENDING", "ACTIVE")
                        .orderByDesc(FamilyDoctorContract::getCreatedAt)
                        .last("LIMIT 1"));
        if (c == null) {
            // 尝试查历史记录
            c = contractMapper.selectOne(
                    new LambdaQueryWrapper<FamilyDoctorContract>()
                            .eq(FamilyDoctorContract::getResidentId, residentId)
                            .orderByDesc(FamilyDoctorContract::getCreatedAt)
                            .last("LIMIT 1"));
        }
        return Result.success(c);
    }

    /** 发起签约申请 */
    @PostMapping("/contract/apply")
    public Result<?> applyContract(@RequestBody FamilyDoctorContract contract) {
        Long residentId = SecurityUtils.getCurrentUserId();

        // 校验：不能有 PENDING 或 ACTIVE 的签约
        Long existing = contractMapper.selectCount(
                new LambdaQueryWrapper<FamilyDoctorContract>()
                        .eq(FamilyDoctorContract::getResidentId, residentId)
                        .in(FamilyDoctorContract::getStatus, "PENDING", "ACTIVE"));
        if (existing > 0) {
            return Result.error(400, "您已有待审核或生效中的签约，不可重复申请");
        }

        contract.setResidentId(residentId);
        contract.setStatus("PENDING");
        contract.setApplyTime(LocalDateTime.now());
        contractMapper.insert(contract);
        return Result.success(contract);
    }

    /** 居民申请解约 */
    @PutMapping("/contract/{id}/cancel")
    public Result<?> cancelContract(@PathVariable Long id,
                                     @RequestBody(required = false) Map<String, String> body) {
        Long residentId = SecurityUtils.getCurrentUserId();
        FamilyDoctorContract c = contractMapper.selectById(id);
        if (c == null || !c.getResidentId().equals(residentId)) {
            return Result.error(403, "无权操作");
        }
        if (!"ACTIVE".equals(c.getStatus())) {
            return Result.error(400, "只有生效中的签约可以解约");
        }
        c.setStatus("CANCELLED");
        c.setCancelReason(body != null ? body.getOrDefault("reason", "居民主动解约") : "居民主动解约");
        contractMapper.updateById(c);
        return Result.success();
    }

    /** 可签约医生列表（按科室） */
    @GetMapping("/doctors")
    public Result<?> availableDoctors(@RequestParam(required = false) String deptCode) {
        String sql = "SELECT s.id AS doctorId, s.name AS doctorName, s.dept_code AS deptCode, " +
                "d.dept_name AS deptName, " +
                "(SELECT COUNT(*) FROM family_doctor_contract fdc WHERE fdc.doctor_id = s.id AND fdc.status = 'ACTIVE') AS contractCount " +
                "FROM chp_admin.staff s LEFT JOIN chp_admin.dept d ON s.dept_code = d.dept_code " +
                "WHERE s.role_code = 'DOCTOR' AND s.status = 1";
        List<Map<String, Object>> doctors;
        if (deptCode != null && !deptCode.isBlank()) {
            sql += " AND s.dept_code = ? ORDER BY contractCount ASC";
            doctors = jdbcTemplate.queryForList(sql, deptCode);
        } else {
            sql += " ORDER BY contractCount ASC";
            doctors = jdbcTemplate.queryForList(sql);
        }
        return Result.success(doctors);
    }

    // ===== 随访相关（修正列名 + @RequestAttribute bug） =====

    /** 我的随访计划 */
    @GetMapping("/plans")
    public Result<?> myPlans() {
        Long residentId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> plans = jdbcTemplate.queryForList(
                "SELECT id, resident_id AS residentId, resident_name AS residentName, " +
                "chronic_type AS chronicType, doctor_id AS doctorId, doctor_name AS doctorName, " +
                "frequency, follow_up_method AS followUpMethod, " +
                "next_follow_date AS nextFollowDate, status, created_at AS createdAt " +
                "FROM chp_admin.follow_up_plan " +
                "WHERE resident_id = ? AND is_deleted = 0 " +
                "ORDER BY next_follow_date ASC", residentId);
        return Result.success(plans);
    }

    /** 随访记录（按计划ID） */
    @GetMapping("/records")
    public Result<?> records(@RequestParam Long planId) {
        Long residentId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> records = jdbcTemplate.queryForList(
                "SELECT id, follow_date AS followDate, follow_method AS followMethod, " +
                "systolic_bp AS systolicBp, diastolic_bp AS diastolicBp, " +
                "fasting_glucose AS fastingGlucose, postprandial_glucose AS postprandialGlucose, " +
                "medication_compliance AS medicationCompliance, " +
                "health_guidance AS healthGuidance, staff_name AS staffName, " +
                "created_at AS createdAt " +
                "FROM chp_admin.follow_up_record " +
                "WHERE plan_id = ? AND resident_id = ? " +
                "ORDER BY follow_date DESC", planId, residentId);
        return Result.success(records);
    }

    /** 健康趋势数据（用于 ECharts） */
    @GetMapping("/trend")
    public Result<?> trend(@RequestParam Long planId,
                           @RequestParam(defaultValue = "50") int limit) {
        Long residentId = SecurityUtils.getCurrentUserId();
        List<Map<String, Object>> data = jdbcTemplate.queryForList(
                "SELECT follow_date AS followDate, " +
                "systolic_bp AS systolicBp, diastolic_bp AS diastolicBp, " +
                "fasting_glucose AS fastingGlucose " +
                "FROM chp_admin.follow_up_record " +
                "WHERE plan_id = ? AND resident_id = ? " +
                "ORDER BY follow_date ASC LIMIT ?", planId, residentId, limit);
        return Result.success(data);
    }
}
