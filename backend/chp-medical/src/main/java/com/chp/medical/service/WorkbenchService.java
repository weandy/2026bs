package com.chp.medical.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.resident.entity.Appointment;
import com.chp.resident.entity.VisitRecord;
import com.chp.resident.mapper.AppointmentMapper;
import com.chp.resident.mapper.VisitRecordMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkbenchService {

    private final AppointmentMapper appointmentMapper;
    private final VisitRecordMapper visitRecordMapper;

    /** 今日待接诊列表 */
    public List<Appointment> todayQueue() {
        Long staffId = SecurityUtils.getCurrentUserId();
        return appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getStaffId, staffId)
                .eq(Appointment::getApptDate, LocalDate.now())
                .in(Appointment::getStatus, 1, 2)
                .orderByAsc(Appointment::getTimePeriod)
                .orderByAsc(Appointment::getCreatedAt));
    }

    /** 叫号 */
    public void callNext(Long appointmentId) {
        Appointment appt = appointmentMapper.selectById(appointmentId);
        if (appt == null) throw new BusinessException(StatusCode.NOT_FOUND, "预约不存在");
        if (appt.getStatus() != 1) throw new BusinessException(StatusCode.BAD_REQUEST, "非待叫号状态");
        appt.setStatus(2);
        appointmentMapper.updateById(appt);
    }

    /** 开始接诊 */
    public VisitRecord startVisit(Long appointmentId) {
        Appointment appt = appointmentMapper.selectById(appointmentId);
        if (appt == null) throw new BusinessException(StatusCode.NOT_FOUND, "预约不存在");

        appt.setStatus(3);
        appointmentMapper.updateById(appt);

        VisitRecord vr = new VisitRecord();
        vr.setResidentId(appt.getResidentId());
        vr.setAppointmentId(appt.getId());
        vr.setDeptCode(appt.getDeptCode());
        vr.setDeptName(appt.getDeptName());
        vr.setStaffId(appt.getStaffId());
        vr.setStaffName(appt.getStaffName());
        vr.setVisitDate(LocalDate.now());
        visitRecordMapper.insert(vr);
        return vr;
    }

    /** 完成接诊 */
    public void completeVisit(Long visitId, String chiefComplaint, String diagnosisNames, String medicalAdvice) {
        VisitRecord vr = visitRecordMapper.selectById(visitId);
        if (vr == null) throw new BusinessException(StatusCode.NOT_FOUND, "就诊记录不存在");
        vr.setChiefComplaint(chiefComplaint);
        vr.setDiagnosisNames(diagnosisNames);
        vr.setMedicalAdvice(medicalAdvice);
        vr.setFinishedAt(LocalDateTime.now());
        visitRecordMapper.updateById(vr);

        if (vr.getAppointmentId() != null) {
            appointmentMapper.update(null, new LambdaUpdateWrapper<Appointment>()
                    .eq(Appointment::getId, vr.getAppointmentId())
                    .set(Appointment::getStatus, 5));
        }
    }

    /** 公屏数据 */
    public List<Appointment> publicScreen(String deptCode) {
        return appointmentMapper.selectList(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getDeptCode, deptCode)
                .eq(Appointment::getApptDate, LocalDate.now())
                .in(Appointment::getStatus, 1, 2, 3)
                .orderByAsc(Appointment::getTimePeriod)
                .orderByAsc(Appointment::getCreatedAt));
    }
}
