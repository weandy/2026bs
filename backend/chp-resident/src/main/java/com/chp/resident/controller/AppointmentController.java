package com.chp.resident.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.Schedule;
import com.chp.admin.entity.ScheduleSlot;
import com.chp.common.result.Result;
import com.chp.resident.dto.AppointmentCreateDTO;
import com.chp.resident.entity.Appointment;
import com.chp.resident.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 预约挂号接口（居民端）
 */
@RestController
@RequestMapping("/resident/appointment")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * 创建预约
     * POST /api/resident/appointment
     */
    @PostMapping
    public Result<Appointment> create(@Valid @RequestBody AppointmentCreateDTO dto) {
        return Result.success(appointmentService.createAppointment(dto));
    }

    /**
     * 取消预约
     * PUT /api/resident/appointment/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestBody Map<String, String> body) {
        appointmentService.cancelAppointment(id, body.get("reason"));
        return Result.success();
    }

    /**
     * 我的预约列表
     * GET /api/resident/appointment?page=1&size=10&status=1
     */
    @GetMapping
    public Result<Page<Appointment>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(appointmentService.getMyAppointments(page, size, status));
    }

    /**
     * 预约详情
     * GET /api/resident/appointment/{id}
     */
    @GetMapping("/{id}")
    public Result<Appointment> detail(@PathVariable Long id) {
        return Result.success(appointmentService.getDetail(id));
    }

    /**
     * 查询可预约排班
     * GET /api/resident/appointment/schedules?deptCode=QKMZ&date=2025-03-20
     */
    @GetMapping("/schedules")
    public Result<List<Schedule>> schedules(
            @RequestParam String deptCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return Result.success(appointmentService.getAvailableSchedules(deptCode, date));
    }

    /**
     * 查询排班号源
     * GET /api/resident/appointment/slots/{scheduleId}
     */
    @GetMapping("/slots/{scheduleId}")
    public Result<List<ScheduleSlot>> slots(@PathVariable Long scheduleId) {
        return Result.success(appointmentService.getSlots(scheduleId));
    }
}
