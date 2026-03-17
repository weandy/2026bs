package com.chp.resident.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.resident.entity.HealthRecord;
import com.chp.resident.mapper.HealthRecordMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 健康档案服务
 */
@Service
@RequiredArgsConstructor
public class HealthRecordService {

    private final HealthRecordMapper healthRecordMapper;

    /**
     * 获取当前居民的健康档案
     */
    public HealthRecord getMyRecord() {
        Long userId = SecurityUtils.getCurrentUserId();
        return healthRecordMapper.selectOne(
                new LambdaQueryWrapper<HealthRecord>().eq(HealthRecord::getResidentId, userId));
    }

    /**
     * 更新健康档案
     */
    public HealthRecord updateRecord(HealthRecord record) {
        Long userId = SecurityUtils.getCurrentUserId();
        HealthRecord existing = healthRecordMapper.selectOne(
                new LambdaQueryWrapper<HealthRecord>().eq(HealthRecord::getResidentId, userId));
        if (existing == null) {
            record.setResidentId(userId);
            healthRecordMapper.insert(record);
            return record;
        }
        record.setId(existing.getId());
        record.setResidentId(userId);
        healthRecordMapper.updateById(record);
        return record;
    }
}
