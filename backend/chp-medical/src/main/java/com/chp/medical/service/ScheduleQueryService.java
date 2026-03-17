package com.chp.medical.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.Schedule;
import com.chp.admin.mapper.ScheduleMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 排班查询服务（医护端）
 */
@Service
@RequiredArgsConstructor
public class ScheduleQueryService {

    private final ScheduleMapper scheduleMapper;

    /** 查询我的排班 */
    public Page<Schedule> mySchedules(int page, int size, LocalDate from, LocalDate to) {
        Long staffId = SecurityUtils.getCurrentUserId();
        LambdaQueryWrapper<Schedule> query = new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getStaffId, staffId)
                .orderByAsc(Schedule::getScheduleDate);
        if (from != null) query.ge(Schedule::getScheduleDate, from);
        if (to != null) query.le(Schedule::getScheduleDate, to);
        return scheduleMapper.selectPage(new Page<>(page, size), query);
    }

    /** 查询科室排班（给管理员/护士组长看） */
    public Page<Schedule> deptSchedules(String deptCode, int page, int size, LocalDate from, LocalDate to) {
        LambdaQueryWrapper<Schedule> query = new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDeptCode, deptCode)
                .orderByAsc(Schedule::getScheduleDate);
        if (from != null) query.ge(Schedule::getScheduleDate, from);
        if (to != null) query.le(Schedule::getScheduleDate, to);
        return scheduleMapper.selectPage(new Page<>(page, size), query);
    }
}
