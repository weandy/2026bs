package com.chp.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.Schedule;
import com.chp.admin.entity.ScheduleSlot;
import com.chp.admin.mapper.ScheduleMapper;
import com.chp.admin.mapper.ScheduleSlotMapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.security.entity.Role;
import com.chp.security.entity.Staff;
import com.chp.security.mapper.admin.RoleMapper;
import com.chp.security.mapper.admin.StaffMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 管理员后台综合服务
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final StaffMapper staffMapper;
    private final RoleMapper roleMapper;
    private final ScheduleMapper scheduleMapper;
    private final ScheduleSlotMapper scheduleSlotMapper;

    // ---- 用户管理 ----

    public Page<Staff> staffList(int page, int size, String keyword) {
        LambdaQueryWrapper<Staff> query = new LambdaQueryWrapper<Staff>()
                .orderByDesc(Staff::getCreatedAt);
        if (keyword != null && !keyword.isBlank()) {
            query.and(q -> q.like(Staff::getName, keyword).or().like(Staff::getUsername, keyword));
        }
        return staffMapper.selectPage(new Page<>(page, size), query);
    }

    public Staff getStaff(Long id) {
        return staffMapper.selectById(id);
    }

    @Transactional
    public Staff createStaff(Staff staff) {
        staff.setPassword(new BCryptPasswordEncoder().encode(staff.getPassword()));
        staffMapper.insert(staff);
        return staff;
    }

    @Transactional
    public void updateStaff(Staff staff) {
        staffMapper.updateById(staff);
    }

    public void resetPassword(Long staffId, String newPwd) {
        Staff staff = staffMapper.selectById(staffId);
        if (staff == null) throw new BusinessException(StatusCode.NOT_FOUND, "用户不存在");
        staff.setPassword(new BCryptPasswordEncoder().encode(newPwd));
        staffMapper.updateById(staff);
    }

    public List<Role> roleList() {
        return roleMapper.selectList(null);
    }

    // ---- 排班管理 ----

    public Page<Schedule> scheduleList(int page, int size, String deptCode, LocalDate from, LocalDate to) {
        LambdaQueryWrapper<Schedule> query = new LambdaQueryWrapper<Schedule>()
                .orderByAsc(Schedule::getScheduleDate);
        if (deptCode != null) query.eq(Schedule::getDeptCode, deptCode);
        if (from != null) query.ge(Schedule::getScheduleDate, from);
        if (to != null) query.le(Schedule::getScheduleDate, to);
        return scheduleMapper.selectPage(new Page<>(page, size), query);
    }

    @Transactional
    public Schedule createSchedule(Schedule schedule, List<ScheduleSlot> slots) {
        scheduleMapper.insert(schedule);
        for (ScheduleSlot slot : slots) {
            slot.setScheduleId(schedule.getId());
            slot.setRemaining(slot.getTotalQuota());
            scheduleSlotMapper.insert(slot);
        }
        return schedule;
    }

    public void stopSchedule(Long scheduleId, String reason) {
        Schedule sch = scheduleMapper.selectById(scheduleId);
        if (sch == null) throw new BusinessException(StatusCode.NOT_FOUND, "排班不存在");
        sch.setIsStopped(1);
        sch.setStopReason(reason);
        scheduleMapper.updateById(sch);
    }
}
