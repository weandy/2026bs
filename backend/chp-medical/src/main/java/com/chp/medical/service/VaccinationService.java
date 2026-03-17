package com.chp.medical.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.common.annotation.AuditLog;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.resident.entity.VaccineAppointment;
import com.chp.resident.entity.VaccineRecord;
import com.chp.resident.mapper.VaccineAppointmentMapper;
import com.chp.resident.mapper.VaccineRecordMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 医护端接种管理服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VaccinationService {

    private final VaccineRecordMapper vaccineRecordMapper;
    private final VaccineAppointmentMapper vaccineApptMapper;

    /**
     * 待接种列表（已预约 status=1 的预约单）
     */
    public Page<VaccineAppointment> pendingList(int page, int size) {
        return vaccineApptMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<VaccineAppointment>()
                        .eq(VaccineAppointment::getStatus, 1)
                        .orderByAsc(VaccineAppointment::getApptDate));
    }

    /**
     * 登记接种
     */
    @Transactional
    public VaccineRecord registerVaccination(VaccineRecord record) {
        record.setStaffId(SecurityUtils.getCurrentUserId());
        record.setStaffName(SecurityUtils.getCurrentUser().getName());
        record.setVaccinatedAt(LocalDateTime.now());
        vaccineRecordMapper.insert(record);

        // 更新预约状态为已接种
        if (record.getApptId() != null) {
            VaccineAppointment appt = vaccineApptMapper.selectById(record.getApptId());
            if (appt != null) {
                appt.setStatus(2); // 已完成
                vaccineApptMapper.updateById(appt);
            }
        }

        log.info("接种登记完成: 居民={}, 疫苗={}, 剂次={}", record.getResidentId(), record.getVaccineName(), record.getDoseNum());
        return record;
    }

    /**
     * 记录不良反应
     */
    public void recordAdverseReaction(Long recordId, String reaction) {
        VaccineRecord record = vaccineRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(StatusCode.NOT_FOUND, "接种记录不存在");
        }
        record.setAdverseReaction(reaction);
        vaccineRecordMapper.updateById(record);
    }

    /**
     * 接种记录分页查询
     */
    public Page<VaccineRecord> recordPage(int page, int size, Long residentId) {
        LambdaQueryWrapper<VaccineRecord> q = new LambdaQueryWrapper<VaccineRecord>()
                .orderByDesc(VaccineRecord::getVaccinatedAt);
        if (residentId != null) {
            q.eq(VaccineRecord::getResidentId, residentId);
        }
        return vaccineRecordMapper.selectPage(new Page<>(page, size), q);
    }
}
