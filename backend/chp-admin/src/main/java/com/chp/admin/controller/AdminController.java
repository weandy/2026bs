package com.chp.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.dto.ScheduleCreateDTO;
import com.chp.admin.dto.ResetPasswordDTO;
import com.chp.admin.dto.StopScheduleDTO;
import com.chp.admin.entity.Schedule;
import com.chp.admin.entity.ScheduleSlot;
import com.chp.admin.service.AdminService;
import com.chp.common.result.Result;
import com.chp.security.entity.Role;
import com.chp.security.entity.Staff;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import com.chp.common.annotation.AuditLog;

import java.time.LocalDate;
import java.util.List;

/**
 * 管理员接口
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ---- 用户管理 ----
    @GetMapping("/staff")
    public Result<Page<Staff>> staffList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(adminService.staffList(page, size, keyword));
    }

    @GetMapping("/staff/{id}")
    public Result<Staff> getStaff(@PathVariable Long id) {
        return Result.success(adminService.getStaff(id));
    }

    @PostMapping("/staff")
    @AuditLog(type = "STAFF_CREATE", module = "staff", description = "创建员工")
    public Result<Staff> createStaff(@RequestBody Staff staff) {
        return Result.success(adminService.createStaff(staff));
    }

    @PutMapping("/staff/{id}")
    @AuditLog(type = "STAFF_UPDATE", module = "staff", description = "更新员工")
    public Result<Void> updateStaff(@PathVariable Long id, @RequestBody Staff staff) {
        staff.setId(id);
        adminService.updateStaff(staff);
        return Result.success();
    }

    @PutMapping("/staff/{id}/reset-password")
    @AuditLog(type = "PASSWORD_RESET", module = "staff", description = "重置密码")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordDTO dto) {
        adminService.resetPassword(id, dto.getPassword());
        return Result.success();
    }

    @GetMapping("/roles")
    public Result<List<Role>> roles() {
        return Result.success(adminService.roleList());
    }

    // ---- 排班管理 ----
    @GetMapping("/schedule")
    public Result<Page<Schedule>> scheduleList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String deptCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return Result.success(adminService.scheduleList(page, size, deptCode, from, to));
    }

    @PostMapping("/schedule")
    public Result<Schedule> createSchedule(@Valid @RequestBody ScheduleCreateDTO dto) {
        Schedule sch = new Schedule();
        sch.setStaffId(dto.getStaffId());
        sch.setStaffName(dto.getStaffName());
        sch.setDeptCode(dto.getDeptCode());
        sch.setDeptName(dto.getDeptName());
        sch.setScheduleDate(LocalDate.parse(dto.getScheduleDate()));

        List<ScheduleSlot> slots = dto.getSlots().stream().map(s -> {
            ScheduleSlot slot = new ScheduleSlot();
            slot.setTimePeriod(s.getTimePeriod());
            slot.setTotalQuota(s.getTotalQuota());
            return slot;
        }).toList();

        return Result.success(adminService.createSchedule(sch, slots));
    }

    @PutMapping("/schedule/{id}/stop")
    public Result<Void> stopSchedule(@PathVariable Long id, @RequestBody StopScheduleDTO dto) {
        adminService.stopSchedule(id, dto.getReason());
        return Result.success();
    }
}
