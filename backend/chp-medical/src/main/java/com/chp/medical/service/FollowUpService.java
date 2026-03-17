package com.chp.medical.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.admin.entity.FollowUpPlan;
import com.chp.admin.entity.FollowUpRecord;
import com.chp.admin.entity.PublicHealthRecord;
import com.chp.admin.mapper.FollowUpPlanMapper;
import com.chp.admin.mapper.FollowUpRecordMapper;
import com.chp.admin.mapper.PublicHealthRecordMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowUpService {

    private final FollowUpPlanMapper planMapper;
    private final FollowUpRecordMapper recordMapper;
    private final PublicHealthRecordMapper publicHealthMapper;

    /** 随访计划分页 */
    public Page<FollowUpPlan> planPage(int page, int size, String chronicType) {
        LambdaQueryWrapper<FollowUpPlan> q = new LambdaQueryWrapper<FollowUpPlan>()
                .orderByAsc(FollowUpPlan::getNextFollowDate);
        if (chronicType != null && !chronicType.isBlank()) {
            q.eq(FollowUpPlan::getChronicType, chronicType);
        }
        return planMapper.selectPage(new Page<>(page, size), q);
    }

    /** 创建随访计划 */
    public FollowUpPlan createPlan(FollowUpPlan plan) {
        plan.setCreatedBy(SecurityUtils.getCurrentUserId());
        plan.setStatus(1);
        planMapper.insert(plan);
        return plan;
    }

    /** 停止随访计划 */
    public void stopPlan(Long planId) {
        FollowUpPlan plan = planMapper.selectById(planId);
        if (plan != null) {
            plan.setStatus(2);
            planMapper.updateById(plan);
        }
    }

    /** 添加随访记录 */
    @Transactional
    public FollowUpRecord addRecord(FollowUpRecord record) {
        record.setStaffId(SecurityUtils.getCurrentUserId());
        record.setStaffName(SecurityUtils.getCurrentUser().getName());
        recordMapper.insert(record);
        // 更新计划的下次随访日期
        if (record.getNextFollowDate() != null) {
            FollowUpPlan plan = planMapper.selectById(record.getPlanId());
            if (plan != null) {
                plan.setNextFollowDate(record.getNextFollowDate());
                planMapper.updateById(plan);
            }
        }
        return record;
    }

    /** 查看计划的随访记录 */
    public List<FollowUpRecord> recordsByPlan(Long planId) {
        return recordMapper.selectList(new LambdaQueryWrapper<FollowUpRecord>()
                .eq(FollowUpRecord::getPlanId, planId)
                .orderByDesc(FollowUpRecord::getFollowDate));
    }

    /** 今日到期计划 */
    public List<FollowUpPlan> todayDuePlans() {
        return planMapper.selectList(new LambdaQueryWrapper<FollowUpPlan>()
                .eq(FollowUpPlan::getStatus, 1)
                .le(FollowUpPlan::getNextFollowDate, LocalDate.now())
                .orderByAsc(FollowUpPlan::getNextFollowDate));
    }

    // ===== 公卫服务 =====

    /** 公卫记录分页 */
    public Page<PublicHealthRecord> publicHealthPage(int page, int size, String serviceType) {
        LambdaQueryWrapper<PublicHealthRecord> q = new LambdaQueryWrapper<PublicHealthRecord>()
                .orderByDesc(PublicHealthRecord::getServiceDate);
        if (serviceType != null && !serviceType.isBlank()) {
            q.eq(PublicHealthRecord::getServiceType, serviceType);
        }
        return publicHealthMapper.selectPage(new Page<>(page, size), q);
    }

    /** 创建公卫记录 */
    public PublicHealthRecord createPublicHealth(PublicHealthRecord record) {
        record.setStaffId(SecurityUtils.getCurrentUserId());
        record.setStaffName(SecurityUtils.getCurrentUser().getName());
        publicHealthMapper.insert(record);
        return record;
    }

    /** 随访趋势数据（按时间正序，用于绘制折线图） */
    public List<FollowUpRecord> trend(Long planId, int limit) {
        if (limit <= 0 || limit > 200) limit = 50;
        return recordMapper.selectList(new LambdaQueryWrapper<FollowUpRecord>()
                .eq(FollowUpRecord::getPlanId, planId)
                .orderByAsc(FollowUpRecord::getFollowDate)
                .last("LIMIT " + limit));
    }
}
