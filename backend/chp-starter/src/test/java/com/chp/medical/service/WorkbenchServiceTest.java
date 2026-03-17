package com.chp.medical.service;

import com.chp.common.exception.BusinessException;
import com.chp.resident.entity.Appointment;
import com.chp.resident.entity.VisitRecord;
import com.chp.resident.mapper.AppointmentMapper;
import com.chp.resident.mapper.VisitRecordMapper;
import com.chp.security.filter.JwtUserDetails;
import com.chp.security.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkbenchService 单元测试")
class WorkbenchServiceTest {

    @Mock private AppointmentMapper appointmentMapper;
    @Mock private VisitRecordMapper visitRecordMapper;

    @InjectMocks private WorkbenchService workbenchService;

    private JwtUserDetails mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new JwtUserDetails(10L, "李医生", "doctor", "QKMZ", "admin");
    }

    @Test
    @DisplayName("叫号成功——待叫号状态")
    void callNext_success() {
        Appointment appt = new Appointment();
        appt.setId(1L);
        appt.setStatus(1); // 待叫号

        when(appointmentMapper.selectById(1L)).thenReturn(appt);
        when(appointmentMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> workbenchService.callNext(1L));
        assertEquals(2, appt.getStatus());
        verify(appointmentMapper).updateById(appt);
    }

    @Test
    @DisplayName("叫号失败——预约不存在")
    void callNext_notFound() {
        when(appointmentMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> workbenchService.callNext(99L));
    }

    @Test
    @DisplayName("叫号失败——非待叫号状态")
    void callNext_invalidStatus() {
        Appointment appt = new Appointment();
        appt.setId(1L);
        appt.setStatus(3); // 已接诊

        when(appointmentMapper.selectById(1L)).thenReturn(appt);
        assertThrows(BusinessException.class, () -> workbenchService.callNext(1L));
    }

    @Test
    @DisplayName("开始接诊——创建就诊记录")
    void startVisit_success() {
        Appointment appt = new Appointment();
        appt.setId(1L);
        appt.setStatus(2);
        appt.setResidentId(100L);
        appt.setDeptCode("NK");
        appt.setDeptName("内科");
        appt.setStaffId(10L);
        appt.setStaffName("李医生");

        when(appointmentMapper.selectById(1L)).thenReturn(appt);
        when(appointmentMapper.updateById(any())).thenReturn(1);
        when(visitRecordMapper.insert(any())).thenReturn(1);

        VisitRecord result = workbenchService.startVisit(1L);

        assertNotNull(result);
        assertEquals(100L, result.getResidentId());
        assertEquals(1L, result.getAppointmentId());
        assertEquals("NK", result.getDeptCode());
        assertEquals(3, appt.getStatus());
    }

    @Test
    @DisplayName("完成接诊——更新就诊记录（无关联预约）")
    void completeVisit_success() {
        VisitRecord vr = new VisitRecord();
        vr.setId(1L);
        vr.setAppointmentId(null); // 无关联预约，跳过 LambdaUpdateWrapper 分支

        when(visitRecordMapper.selectById(1L)).thenReturn(vr);
        when(visitRecordMapper.updateById(any())).thenReturn(1);

        workbenchService.completeVisit(1L, "头痛3天", "偏头痛", "口服止痛药");

        assertEquals("头痛3天", vr.getChiefComplaint());
        assertEquals("偏头痛", vr.getDiagnosisNames());
        assertNotNull(vr.getFinishedAt());
        verify(visitRecordMapper).updateById(vr);
        verify(appointmentMapper, never()).update(any(), any());
    }

    @Test
    @DisplayName("完成接诊失败——就诊记录不存在")
    void completeVisit_notFound() {
        when(visitRecordMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () ->
            workbenchService.completeVisit(99L, "头痛", "偏头痛", "止痛药"));
    }
}
