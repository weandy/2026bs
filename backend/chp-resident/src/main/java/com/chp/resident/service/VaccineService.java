package com.chp.resident.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.resident.entity.VaccineAppointment;
import com.chp.resident.entity.VaccineRecord;
import com.chp.resident.mapper.VaccineAppointmentMapper;
import com.chp.resident.mapper.VaccineRecordMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VaccineService {

    private final VaccineAppointmentMapper vaccineApptMapper;
    private final VaccineRecordMapper vaccineRecordMapper;

    public VaccineAppointment createAppointment(VaccineAppointment appt) {
        Long userId = SecurityUtils.getCurrentUserId();
        appt.setResidentId(userId);
        appt.setStatus(1);
        vaccineApptMapper.insert(appt);
        return appt;
    }

    public void cancelAppointment(Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        VaccineAppointment appt = vaccineApptMapper.selectById(id);
        if (appt == null || !appt.getResidentId().equals(userId)) {
            throw new BusinessException(StatusCode.FORBIDDEN, "无权操作");
        }
        appt.setStatus(3);
        vaccineApptMapper.updateById(appt);
    }

    public Page<VaccineAppointment> myAppointments(int page, int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return vaccineApptMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<VaccineAppointment>()
                        .eq(VaccineAppointment::getResidentId, userId)
                        .orderByDesc(VaccineAppointment::getCreatedAt));
    }

    public List<VaccineRecord> myRecords() {
        Long userId = SecurityUtils.getCurrentUserId();
        return vaccineRecordMapper.selectList(
                new LambdaQueryWrapper<VaccineRecord>()
                        .eq(VaccineRecord::getResidentId, userId)
                        .orderByDesc(VaccineRecord::getVaccinatedAt));
    }
}
