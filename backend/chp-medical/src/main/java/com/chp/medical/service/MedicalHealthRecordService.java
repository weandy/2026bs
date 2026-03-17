package com.chp.medical.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.resident.entity.HealthRecord;
import com.chp.resident.mapper.HealthRecordMapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 健康档案管理服务（医护端 - 可查看所有居民档案）
 */
@Service("medicalHealthRecordService")
@RequiredArgsConstructor
public class MedicalHealthRecordService {

    private final HealthRecordMapper healthRecordMapper;

    /** 查看指定居民的健康档案 */
    public HealthRecord getByResidentId(Long residentId) {
        HealthRecord record = healthRecordMapper.selectOne(
                new LambdaQueryWrapper<HealthRecord>().eq(HealthRecord::getResidentId, residentId));
        if (record == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "该居民暂无健康档案");
        }
        return record;
    }

    /** 更新居民健康档案 */
    public HealthRecord updateRecord(Long residentId, HealthRecord record) {
        HealthRecord existing = healthRecordMapper.selectOne(
                new LambdaQueryWrapper<HealthRecord>().eq(HealthRecord::getResidentId, residentId));
        if (existing == null) {
            record.setResidentId(residentId);
            healthRecordMapper.insert(record);
            return record;
        }
        record.setId(existing.getId());
        record.setResidentId(residentId);
        healthRecordMapper.updateById(record);
        return record;
    }
}
