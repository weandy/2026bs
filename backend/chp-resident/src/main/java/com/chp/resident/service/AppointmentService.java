package com.chp.resident.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.Schedule;
import com.chp.admin.entity.ScheduleSlot;
import com.chp.admin.mapper.ScheduleMapper;
import com.chp.admin.mapper.ScheduleSlotMapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.resident.dto.AppointmentCreateDTO;
import com.chp.resident.entity.Appointment;
import com.chp.resident.mapper.AppointmentMapper;
import com.chp.security.filter.JwtUserDetails;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 预约挂号服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final ScheduleSlotMapper scheduleSlotMapper;
    private final ScheduleMapper scheduleMapper;

    private static final AtomicLong SEQ = new AtomicLong(System.currentTimeMillis() % 100000);

    /**
     * 创建预约
     */
    public Appointment createAppointment(AppointmentCreateDTO dto) {
        JwtUserDetails user = SecurityUtils.getCurrentUser();
        if (user == null) {
            throw new BusinessException(StatusCode.UNAUTHORIZED, "请先登录");
        }

        // 检查重复预约（同居民、同科室、同日期）
        long count = appointmentMapper.selectCount(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getResidentId, user.getUserId())
                .eq(Appointment::getDeptCode, dto.getDeptCode())
                .eq(Appointment::getApptDate, dto.getApptDate())
                .in(Appointment::getStatus, 1, 2));
        if (count > 0) {
            throw new BusinessException(StatusCode.DUPLICATE_APPOINTMENT, "您当天已有该科室的预约");
        }

        // 乐观锁扣减号源
        ScheduleSlot slot = scheduleSlotMapper.selectById(dto.getSlotId());
        if (slot == null || slot.getRemaining() <= 0) {
            throw new BusinessException(StatusCode.SLOT_FULL, "该时段号源已约满");
        }

        int rows = scheduleSlotMapper.update(null, new LambdaUpdateWrapper<ScheduleSlot>()
                .eq(ScheduleSlot::getId, slot.getId())
                .eq(ScheduleSlot::getVersion, slot.getVersion())
                .gt(ScheduleSlot::getRemaining, 0)
                .set(ScheduleSlot::getRemaining, slot.getRemaining() - 1)
                .set(ScheduleSlot::getVersion, slot.getVersion() + 1));
        if (rows == 0) {
            throw new BusinessException(StatusCode.SLOT_FULL, "号源已被他人抢占，请重试");
        }

        // 生成就诊号
        String apptNo = "APT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + String.format("%05d", SEQ.incrementAndGet() % 100000);

        Appointment appt = new Appointment();
        appt.setApptNo(apptNo);
        appt.setResidentId(user.getUserId());
        appt.setSlotId(dto.getSlotId());
        appt.setDeptCode(dto.getDeptCode());
        appt.setDeptName(dto.getDeptName());
        appt.setStaffId(dto.getStaffId());
        appt.setStaffName(dto.getStaffName());
        appt.setApptDate(dto.getApptDate());
        appt.setTimePeriod(dto.getTimePeriod());
        appt.setPatientName(dto.getPatientName());
        appt.setPatientPhone(dto.getPatientPhone());
        appt.setSymptomDesc(dto.getSymptomDesc());
        appt.setStatus(1);
        appointmentMapper.insert(appt);

        return appt;
    }

    /**
     * 取消预约
     */
    public void cancelAppointment(Long id, String reason) {
        JwtUserDetails user = SecurityUtils.getCurrentUser();
        Appointment appt = appointmentMapper.selectById(id);
        if (appt == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "预约不存在");
        }
        if (!appt.getResidentId().equals(user.getUserId())) {
            throw new BusinessException(StatusCode.FORBIDDEN, "无权操作");
        }
        if (appt.getStatus() != 1) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "当前状态不可取消");
        }

        appt.setStatus(4);
        appt.setCancelReason(reason);
        appointmentMapper.updateById(appt);

        // 归还号源
        ScheduleSlot slot = scheduleSlotMapper.selectById(appt.getSlotId());
        if (slot != null) {
            scheduleSlotMapper.update(null, new LambdaUpdateWrapper<ScheduleSlot>()
                    .eq(ScheduleSlot::getId, slot.getId())
                    .eq(ScheduleSlot::getVersion, slot.getVersion())
                    .set(ScheduleSlot::getRemaining, slot.getRemaining() + 1)
                    .set(ScheduleSlot::getVersion, slot.getVersion() + 1));
        }
    }

    /**
     * 查询居民的预约列表
     */
    public Page<Appointment> getMyAppointments(int page, int size, Integer status) {
        JwtUserDetails user = SecurityUtils.getCurrentUser();
        Page<Appointment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Appointment> query = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getResidentId, user.getUserId())
                .orderByDesc(Appointment::getCreatedAt);
        if (status != null) {
            query.eq(Appointment::getStatus, status);
        }
        return appointmentMapper.selectPage(pageParam, query);
    }

    /**
     * 查询预约详情
     */
    public Appointment getDetail(Long id) {
        Appointment appt = appointmentMapper.selectById(id);
        if (appt == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "预约不存在");
        }
        return appt;
    }

    /**
     * 查询可预约的排班（指定日期+科室）
     */
    public List<Schedule> getAvailableSchedules(String deptCode, LocalDate date) {
        return scheduleMapper.selectList(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDeptCode, deptCode)
                .eq(Schedule::getScheduleDate, date)
                .eq(Schedule::getIsStopped, 0));
    }

    /**
     * 查询排班对应的时段号源
     */
    public List<ScheduleSlot> getSlots(Long scheduleId) {
        return scheduleSlotMapper.selectList(new LambdaQueryWrapper<ScheduleSlot>()
                .eq(ScheduleSlot::getScheduleId, scheduleId)
                .gt(ScheduleSlot::getRemaining, 0));
    }
}
