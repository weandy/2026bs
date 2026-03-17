package com.chp.resident.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chp.resident.entity.Appointment;
import com.chp.resident.mapper.AppointmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务：预约过期自动取消 + 号源超时释放
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentExpireTask {

    private final AppointmentMapper appointmentMapper;

    /**
     * 每小时检查：过了就诊日期仍为"待就诊"的预约 → 标记为"过期"
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void expireOverdueAppointments() {
        log.info("[定时任务] 检查过期预约...");
        // status=1 待就诊, 4=过期
        int updated = appointmentMapper.update(null,
                new LambdaUpdateWrapper<Appointment>()
                        .eq(Appointment::getStatus, 1)
                        .lt(Appointment::getApptDate, LocalDate.now())
                        .set(Appointment::getStatus, 4)
                        .set(Appointment::getUpdatedAt, LocalDateTime.now()));
        if (updated > 0) {
            log.info("[定时任务] 已将 {} 条过期预约标记为过期", updated);
        }
    }

    /**
     * 每30分钟检查：创建超过30分钟仍为"待确认"状态(status=0)的预约 → 自动取消释放号源
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void releaseTimeoutAppointments() {
        log.info("[定时任务] 检查超时未确认预约...");
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(30);
        List<Appointment> timeoutList = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getStatus, 0)
                        .lt(Appointment::getCreatedAt, timeout));
        for (Appointment appt : timeoutList) {
            appt.setStatus(3); // 3=已取消
            appt.setCancelReason("系统自动取消：超时未确认");
            appointmentMapper.updateById(appt);
        }
        if (!timeoutList.isEmpty()) {
            log.info("[定时任务] 已自动取消 {} 条超时预约", timeoutList.size());
        }
    }
}
