package com.chp.medical.controller;

import com.chp.common.result.Result;
import com.chp.resident.entity.HealthRecord;
import com.chp.medical.service.MedicalHealthRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 健康档案管理接口（医护端）
 */
@RestController
@RequestMapping("/medical/health-record")
@RequiredArgsConstructor
public class MedicalHealthRecordController {

    private final MedicalHealthRecordService healthRecordService;

    @GetMapping("/{residentId}")
    public Result<HealthRecord> get(@PathVariable Long residentId) {
        return Result.success(healthRecordService.getByResidentId(residentId));
    }

    @PutMapping("/{residentId}")
    public Result<HealthRecord> update(@PathVariable Long residentId, @RequestBody HealthRecord record) {
        return Result.success(healthRecordService.updateRecord(residentId, record));
    }
}
