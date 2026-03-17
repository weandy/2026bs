package com.chp.medical.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.Schedule;
import com.chp.common.result.Result;
import com.chp.medical.service.ScheduleQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 排班查询接口（医护端）
 */
@RestController
@RequestMapping("/medical/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleQueryService scheduleQueryService;

    @GetMapping("/my")
    public Result<Page<Schedule>> mySchedules(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return Result.success(scheduleQueryService.mySchedules(page, size, from, to));
    }

    @GetMapping("/dept/{deptCode}")
    public Result<Page<Schedule>> deptSchedules(
            @PathVariable String deptCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return Result.success(scheduleQueryService.deptSchedules(deptCode, page, size, from, to));
    }
}
