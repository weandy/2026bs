package com.chp.resident.service;

import com.chp.admin.entity.ScheduleSlot;
import com.chp.admin.mapper.ScheduleMapper;
import com.chp.admin.mapper.ScheduleSlotMapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.resident.dto.AppointmentCreateDTO;
import com.chp.resident.entity.Appointment;
import com.chp.resident.mapper.AppointmentMapper;
import com.chp.security.filter.JwtUserDetails;
import com.chp.security.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService 单元测试")
class AppointmentServiceTest {

    @Mock private AppointmentMapper appointmentMapper;
    @Mock private ScheduleSlotMapper scheduleSlotMapper;
    @Mock private ScheduleMapper scheduleMapper;

    @InjectMocks private AppointmentService appointmentService;

    private AppointmentCreateDTO mockDTO;
    private JwtUserDetails mockUser;
    private ScheduleSlot mockSlot;

    @BeforeEach
    void setUp() {
        mockUser = new JwtUserDetails(100L, "张三", "resident", null, "resident");

        mockDTO = new AppointmentCreateDTO();
        mockDTO.setSlotId(1L);
        mockDTO.setDeptCode("QKMZ");
        mockDTO.setDeptName("全科门诊");
        mockDTO.setStaffId(10L);
        mockDTO.setStaffName("李医生");
        mockDTO.setApptDate(LocalDate.now().plusDays(1));
        mockDTO.setTimePeriod(1);
        mockDTO.setPatientName("张三");
        mockDTO.setPatientPhone("13800138000");

        mockSlot = new ScheduleSlot();
        mockSlot.setId(1L);
        mockSlot.setRemaining(5);
        mockSlot.setVersion(0);
    }

    @Test
    @Disabled("需要 MyBatis-Plus TableInfo 初始化，移入集成测试")
    @DisplayName("创建预约成功")
    void createAppointment_success() {
        try (MockedStatic<SecurityUtils> secUtils = mockStatic(SecurityUtils.class)) {
            secUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(appointmentMapper.selectCount(any())).thenReturn(0L);
            when(scheduleSlotMapper.selectById(1L)).thenReturn(mockSlot);
            when(scheduleSlotMapper.update(any(), any())).thenReturn(1);
            when(appointmentMapper.insert(any())).thenReturn(1);

            Appointment result = appointmentService.createAppointment(mockDTO);

            assertNotNull(result);
            assertEquals(1, result.getStatus());
            assertTrue(result.getApptNo().startsWith("APT"));
            verify(appointmentMapper).insert(any());
        }
    }

    @Test
    @DisplayName("创建预约 - 重复预约")
    void createAppointment_duplicate() {
        try (MockedStatic<SecurityUtils> secUtils = mockStatic(SecurityUtils.class)) {
            secUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(appointmentMapper.selectCount(any())).thenReturn(1L);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> appointmentService.createAppointment(mockDTO));
            assertEquals(StatusCode.DUPLICATE_APPOINTMENT, ex.getCode());
        }
    }

    @Test
    @DisplayName("创建预约 - 号源已满")
    void createAppointment_slotFull() {
        try (MockedStatic<SecurityUtils> secUtils = mockStatic(SecurityUtils.class)) {
            secUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(appointmentMapper.selectCount(any())).thenReturn(0L);
            mockSlot.setRemaining(0);
            when(scheduleSlotMapper.selectById(1L)).thenReturn(mockSlot);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> appointmentService.createAppointment(mockDTO));
            assertEquals(StatusCode.SLOT_FULL, ex.getCode());
        }
    }

    @Test
    @Disabled("需要 MyBatis-Plus TableInfo 初始化，移入集成测试")
    @DisplayName("创建预约 - 乐观锁冲突")
    void createAppointment_optimisticLockFail() {
        try (MockedStatic<SecurityUtils> secUtils = mockStatic(SecurityUtils.class)) {
            secUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(appointmentMapper.selectCount(any())).thenReturn(0L);
            when(scheduleSlotMapper.selectById(1L)).thenReturn(mockSlot);
            when(scheduleSlotMapper.update(any(), any())).thenReturn(0); // 乐观锁失败

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> appointmentService.createAppointment(mockDTO));
            assertEquals(StatusCode.SLOT_FULL, ex.getCode());
        }
    }

    @Test
    @DisplayName("取消预约 - 预约不存在")
    void cancelAppointment_notFound() {
        try (MockedStatic<SecurityUtils> secUtils = mockStatic(SecurityUtils.class)) {
            secUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            when(appointmentMapper.selectById(99L)).thenReturn(null);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> appointmentService.cancelAppointment(99L, "不想去了"));
            assertEquals(StatusCode.NOT_FOUND, ex.getCode());
        }
    }

    @Test
    @DisplayName("取消预约 - 无权操作")
    void cancelAppointment_forbidden() {
        try (MockedStatic<SecurityUtils> secUtils = mockStatic(SecurityUtils.class)) {
            secUtils.when(SecurityUtils::getCurrentUser).thenReturn(mockUser);
            Appointment appt = new Appointment();
            appt.setId(1L);
            appt.setResidentId(999L); // 不是当前用户的
            appt.setStatus(1);
            when(appointmentMapper.selectById(1L)).thenReturn(appt);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> appointmentService.cancelAppointment(1L, "取消"));
            assertEquals(StatusCode.FORBIDDEN, ex.getCode());
        }
    }
}
