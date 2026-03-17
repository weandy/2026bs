package com.chp.medical.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.common.annotation.AuditLog;
import com.chp.common.result.Result;
import com.chp.medical.service.VaccinationService;
import com.chp.resident.entity.VaccineAppointment;
import com.chp.resident.entity.VaccineRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 医护端接种管理
 */
@RestController
@RequestMapping("/medical/vaccination")
@RequiredArgsConstructor
public class VaccinationController {

    private final VaccinationService vaccinationService;

    /** 待接种列表 */
    @GetMapping("/pending")
    public Result<Page<VaccineAppointment>> pending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(vaccinationService.pendingList(page, size));
    }

    /** 登记接种 */
    @PostMapping("/register")
    @AuditLog(type = "VACCINE_REGISTER", module = "vaccination", description = "登记接种")
    public Result<VaccineRecord> register(@RequestBody VaccineRecord record) {
        return Result.success(vaccinationService.registerVaccination(record));
    }

    /** 记录不良反应 */
    @PutMapping("/{recordId}/adverse")
    @AuditLog(type = "VACCINE_ADVERSE", module = "vaccination", description = "记录不良反应")
    public Result<Void> adverse(@PathVariable Long recordId, @RequestBody Map<String, String> body) {
        vaccinationService.recordAdverseReaction(recordId, body.get("reaction"));
        return Result.success();
    }

    /** 接种记录分页 */
    @GetMapping("/records")
    public Result<Page<VaccineRecord>> records(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long residentId) {
        return Result.success(vaccinationService.recordPage(page, size, residentId));
    }
}
