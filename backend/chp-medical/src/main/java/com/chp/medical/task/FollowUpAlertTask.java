package com.chp.medical.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.admin.entity.FollowUpPlan;
import com.chp.admin.mapper.FollowUpPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 定时任务：随访预警 — 检查即将到期的随访计划并发出提醒
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FollowUpAlertTask {

    private final FollowUpPlanMapper followUpPlanMapper;

    /**
     * 每天早上8点检查：未来3天内需要随访的计划 → 记录预警日志
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkUpcomingFollowUps() {
        log.info("[定时任务] 检查即将到期的随访计划...");
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);

        List<FollowUpPlan> plans = followUpPlanMapper.selectList(
                new LambdaQueryWrapper<FollowUpPlan>()
                        .eq(FollowUpPlan::getStatus, 1) // 进行中
                        .le(FollowUpPlan::getNextFollowDate, threeDaysLater)
                        .ge(FollowUpPlan::getNextFollowDate, today));

        for (FollowUpPlan plan : plans) {
            log.warn("[随访预警] 居民ID={}, 类型={}, 下次随访日期={}, 需尽快安排随访",
                    plan.getResidentId(), plan.getChronicType(), plan.getNextFollowDate());
            // TODO: 后续可以推送消息到居民端
        }
        log.info("[定时任务] 随访预警完成，发现 {} 项即将到期", plans.size());
    }

    /**
     * 每天凌晨检查：已过期未随访的计划 → 标记为已过期
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void markOverdueFollowUps() {
        log.info("[定时任务] 检查过期随访计划...");
        LocalDate yesterday = LocalDate.now().minusDays(1);

        List<FollowUpPlan> overdue = followUpPlanMapper.selectList(
                new LambdaQueryWrapper<FollowUpPlan>()
                        .eq(FollowUpPlan::getStatus, 1) // 进行中
                        .lt(FollowUpPlan::getNextFollowDate, yesterday));

        for (FollowUpPlan plan : overdue) {
            plan.setStatus(3); // 标记过期
            followUpPlanMapper.updateById(plan);
        }
        if (!overdue.isEmpty()) {
            log.info("[定时任务] 已将 {} 条过期随访计划标记为过期", overdue.size());
        }
    }
}
