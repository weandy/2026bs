package com.chp.medical.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.admin.entity.FollowUpPlan;
import com.chp.admin.mapper.FollowUpPlanMapper;
import com.chp.resident.entity.Message;
import com.chp.resident.entity.VaccineAppointment;
import com.chp.resident.mapper.MessageMapper;
import com.chp.resident.mapper.VaccineAppointmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息推送定时任务
 * 将随访提醒、接种提醒、复诊提醒写入 message 表
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessagePushTask {

    private final FollowUpPlanMapper followUpPlanMapper;
    private final VaccineAppointmentMapper vaccineApptMapper;
    private final MessageMapper messageMapper;

    /**
     * 随访提醒：每日 8:30 推送次日到期的随访计划
     */
    @Scheduled(cron = "0 30 8 * * *")
    public void pushFollowUpReminder() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<FollowUpPlan> plans = followUpPlanMapper.selectList(
                new LambdaQueryWrapper<FollowUpPlan>()
                        .eq(FollowUpPlan::getStatus, 1)
                        .eq(FollowUpPlan::getNextFollowDate, tomorrow));

        for (FollowUpPlan plan : plans) {
            Message msg = new Message();
            msg.setResidentId(plan.getResidentId());
            msg.setMsgType("follow_up");
            msg.setTitle("随访提醒");
            msg.setContent("您有一项" + plan.getChronicType() + "随访计划将于明天到期，请按时前往社区卫生服务中心。");
            msg.setRelatedId(plan.getId());
            msg.setIsRead(0);
            msg.setCreatedAt(LocalDateTime.now());
            messageMapper.insert(msg);
        }
        log.info("随访提醒推送完成，共{}条", plans.size());
    }

    /**
     * 接种提醒：每日 9:00 推送当日预约的接种
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void pushVaccineReminder() {
        LocalDate today = LocalDate.now();
        List<VaccineAppointment> appts = vaccineApptMapper.selectList(
                new LambdaQueryWrapper<VaccineAppointment>()
                        .eq(VaccineAppointment::getStatus, 1)
                        .eq(VaccineAppointment::getApptDate, today));

        for (VaccineAppointment appt : appts) {
            Message msg = new Message();
            msg.setResidentId(appt.getResidentId());
            msg.setMsgType("vaccine");
            msg.setTitle("接种提醒");
            msg.setContent("您今日有" + appt.getVaccineName() + "第" + appt.getDoseNum() + "剂次接种预约，请按时前往。");
            msg.setRelatedId(appt.getId());
            msg.setIsRead(0);
            msg.setCreatedAt(LocalDateTime.now());
            messageMapper.insert(msg);
        }
        log.info("接种提醒推送完成，共{}条", appts.size());
    }
}
