package com.chp.resident.controller;

import com.chp.common.result.Result;
import com.chp.resident.entity.HealthRecord;
import com.chp.resident.service.HealthRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 健康档案接口（居民端）
 */
@RestController
@RequestMapping("/resident/health-record")
@RequiredArgsConstructor
public class HealthRecordController {

    private final HealthRecordService healthRecordService;

    /**
     * 查看我的健康档案
     */
    @GetMapping
    public Result<HealthRecord> getMyRecord() {
        return Result.success(healthRecordService.getMyRecord());
    }

    /**
     * 更新健康档案
     */
    @PutMapping
    public Result<HealthRecord> update(@RequestBody HealthRecord record) {
        return Result.success(healthRecordService.updateRecord(record));
    }
}
