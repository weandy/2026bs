package com.chp.medical.controller;

import com.chp.common.result.Result;
import com.chp.resident.entity.HealthRecord;
import com.chp.medical.service.MedicalHealthRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 健康档案管理接口（医护端）
 */
@RestController
@RequestMapping("/medical/health-record")
@RequiredArgsConstructor
public class MedicalHealthRecordController {

    private final MedicalHealthRecordService healthRecordService;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/{residentId}")
    public Result<HealthRecord> get(@PathVariable Long residentId) {
        return Result.success(healthRecordService.getByResidentId(residentId));
    }

    @PutMapping("/{residentId}")
    public Result<HealthRecord> update(@PathVariable Long residentId, @RequestBody HealthRecord record) {
        return Result.success(healthRecordService.updateRecord(residentId, record));
    }

    /** GET /medical/residents/search?keyword=xxx — 按姓名/手机号/ID搜索居民（供档案搜索使用） */
    @GetMapping("/residents/search")
    public Result<List<Map<String, Object>>> searchResidents(@RequestParam String keyword) {
        try {
            boolean isNumeric = keyword.matches("\\d+");
            String sql = "SELECT id, name, phone, birth_date FROM sys_resident WHERE " +
                "(name LIKE ? OR phone LIKE ?" + (isNumeric ? " OR id = ?" : "") + ") LIMIT 20";
            List<Object> params = new java.util.ArrayList<>();
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            if (isNumeric) params.add(Long.parseLong(keyword));
            return Result.success(jdbcTemplate.queryForList(sql, params.toArray()));
        } catch (Exception e) {
            return Result.success(java.util.Collections.emptyList());
        }
    }
}
