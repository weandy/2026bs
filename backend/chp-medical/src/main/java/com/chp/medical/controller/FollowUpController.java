package com.chp.medical.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.FollowUpPlan;
import com.chp.admin.entity.FollowUpRecord;
import com.chp.admin.entity.PublicHealthRecord;
import com.chp.common.result.Result;
import com.chp.medical.service.FollowUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.chp.common.annotation.AuditLog;

import java.util.List;

@RestController
@RequestMapping("/medical")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;

    // ===== 随访计划 =====

    @GetMapping("/follow-up/plan")
    public Result<Page<FollowUpPlan>> planList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String chronicType) {
        return Result.success(followUpService.planPage(page, size, chronicType));
    }

    @PostMapping("/follow-up/plan")
    @AuditLog(type = "FOLLOWUP_CREATE", module = "followUp", description = "创建随访计划")
    public Result<FollowUpPlan> createPlan(@RequestBody FollowUpPlan plan) {
        return Result.success(followUpService.createPlan(plan));
    }

    @PutMapping("/follow-up/plan/{id}/stop")
    @AuditLog(type = "FOLLOWUP_STOP", module = "followUp", description = "停止随访计划")
    public Result<Void> stopPlan(@PathVariable Long id) {
        followUpService.stopPlan(id);
        return Result.success();
    }

    @GetMapping("/follow-up/plan/today-due")
    public Result<List<FollowUpPlan>> todayDue() {
        return Result.success(followUpService.todayDuePlans());
    }

    // ===== 随访记录 =====

    @PostMapping("/follow-up/record")
    @AuditLog(type = "FOLLOWUP_RECORD", module = "followUp", description = "录入随访记录")
    public Result<FollowUpRecord> addRecord(@RequestBody FollowUpRecord record) {
        return Result.success(followUpService.addRecord(record));
    }

    @GetMapping("/follow-up/record/{planId}")
    public Result<List<FollowUpRecord>> recordList(@PathVariable Long planId) {
        return Result.success(followUpService.recordsByPlan(planId));
    }

    @GetMapping("/follow-up/trend/{planId}")
    public Result<List<FollowUpRecord>> trend(
            @PathVariable Long planId,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(followUpService.trend(planId, limit));
    }

    // ===== 公卫服务 =====

    @GetMapping("/public-health")
    public Result<Page<PublicHealthRecord>> publicHealthList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String serviceType) {
        return Result.success(followUpService.publicHealthPage(page, size, serviceType));
    }

    @PostMapping("/public-health")
    @AuditLog(type = "PUBLIC_HEALTH", module = "publicHealth", description = "创建公卫记录")
    public Result<PublicHealthRecord> createPublicHealth(@RequestBody PublicHealthRecord record) {
        return Result.success(followUpService.createPublicHealth(record));
    }
}
