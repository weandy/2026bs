package com.chp.medical.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.resident.entity.Appointment;
import com.chp.resident.entity.HealthRecord;
import com.chp.resident.entity.VisitRecord;
import com.chp.resident.mapper.AppointmentMapper;
import com.chp.resident.mapper.HealthRecordMapper;
import com.chp.resident.mapper.VisitRecordMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkbenchService {

    private final AppointmentMapper appointmentMapper;
    private final VisitRecordMapper visitRecordMapper;
    private final HealthRecordMapper healthRecordMapper;

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

    /** 完成接诊 —— 返回随访建议（创新点②） */
    public Map<String, Object> completeVisit(Long visitId, String chiefComplaint, String diagnosisNames, String medicalAdvice) {
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

        // 随访建议：检测慢病标签
        Map<String, Object> result = new HashMap<>();
        result.put("visitId", visitId);
        result.put("residentId", vr.getResidentId());

        try {
            HealthRecord hr = healthRecordMapper.selectOne(
                    new LambdaQueryWrapper<HealthRecord>().eq(HealthRecord::getResidentId, vr.getResidentId()));
            if (hr != null && hr.getChronicTags() != null && !hr.getChronicTags().isBlank()) {
                String[] tags = hr.getChronicTags().split(",");
                String primaryChronic = tags[0].trim();
                Map<String, Object> suggestion = buildFollowUpSuggestion(primaryChronic, vr);
                if (suggestion != null) {
                    result.put("followUpSuggestion", suggestion);
                }
            }
        } catch (Exception e) {
            log.warn("生成随访建议失败", e);
        }

        return result;
    }

    /** 随访模板匹配（硬编码，覆盖主要慢病） */
    private Map<String, Object> buildFollowUpSuggestion(String chronicType, VisitRecord vr) {
        Map<String, Object> suggestion = new HashMap<>();
        suggestion.put("residentId", vr.getResidentId());
        suggestion.put("chronicType", chronicType);
        suggestion.put("doctorId", vr.getStaffId());
        suggestion.put("doctorName", vr.getStaffName());
        suggestion.put("followUpMethod", 1); // 1=电话

        switch (chronicType) {
            case "hypertension":
                suggestion.put("frequency", 30);
                suggestion.put("nextFollowDate", LocalDate.now().plusDays(30));
                suggestion.put("focusItems", "监测血压，评估降压药效果，生活方式指导");
                suggestion.put("chronicLabel", "高血压");
                break;
            case "diabetes":
                suggestion.put("frequency", 30);
                suggestion.put("nextFollowDate", LocalDate.now().plusDays(30));
                suggestion.put("focusItems", "监测血糖，评估饮食控制与运动，用药依从性");
                suggestion.put("chronicLabel", "糖尿病");
                break;
            case "chd":
                suggestion.put("frequency", 60);
                suggestion.put("nextFollowDate", LocalDate.now().plusDays(60));
                suggestion.put("focusItems", "心电图复查，用药依从性评估，胸闷胸痛频率");
                suggestion.put("chronicLabel", "冠心病");
                break;
            case "copd":
                suggestion.put("frequency", 90);
                suggestion.put("nextFollowDate", LocalDate.now().plusDays(90));
                suggestion.put("focusItems", "肺功能评估，吸入剂使用，急性加重频率");
                suggestion.put("chronicLabel", "慢阻肺");
                break;
            case "stroke":
                suggestion.put("frequency", 30);
                suggestion.put("nextFollowDate", LocalDate.now().plusDays(30));
                suggestion.put("focusItems", "康复训练评估，二级预防依从性，新发症状监测");
                suggestion.put("chronicLabel", "脑卒中");
                break;
            default:
                return null;
        }
        return suggestion;
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
