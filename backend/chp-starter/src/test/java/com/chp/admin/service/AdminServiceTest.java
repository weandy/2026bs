package com.chp.admin.service;

import com.chp.admin.entity.Schedule;
import com.chp.admin.entity.ScheduleSlot;
import com.chp.admin.mapper.ScheduleMapper;
import com.chp.admin.mapper.ScheduleSlotMapper;
import com.chp.common.exception.BusinessException;
import com.chp.security.entity.Role;
import com.chp.security.entity.Staff;
import com.chp.security.mapper.admin.RoleMapper;
import com.chp.security.mapper.admin.StaffMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService 单元测试")
class AdminServiceTest {

    @Mock private StaffMapper staffMapper;
    @Mock private RoleMapper roleMapper;
    @Mock private ScheduleMapper scheduleMapper;
    @Mock private ScheduleSlotMapper scheduleSlotMapper;

    @InjectMocks private AdminService adminService;

    // ---- 用户管理 ----

    @Test
    @DisplayName("获取员工——存在时返回对象")
    void getStaff_success() {
        Staff staff = new Staff();
        staff.setId(1L);
        staff.setName("张三");
        when(staffMapper.selectById(1L)).thenReturn(staff);

        Staff result = adminService.getStaff(1L);
        assertNotNull(result);
        assertEquals("张三", result.getName());
    }

    @Test
    @DisplayName("创建员工——密码加密后插入")
    void createStaff_encodesPassword() {
        Staff staff = new Staff();
        staff.setUsername("zhangsan");
        staff.setName("张三");
        staff.setPassword("123456");
        when(staffMapper.insert(any())).thenReturn(1);

        Staff result = adminService.createStaff(staff);
        assertNotNull(result.getPassword());
        assertNotEquals("123456", result.getPassword()); // 密码已加密
        verify(staffMapper).insert(staff);
    }

    @Test
    @DisplayName("更新员工——调用 updateById")
    void updateStaff_success() {
        Staff staff = new Staff();
        staff.setId(1L);
        staff.setName("李四");
        when(staffMapper.updateById(staff)).thenReturn(1);

        assertDoesNotThrow(() -> adminService.updateStaff(staff));
        verify(staffMapper).updateById(staff);
    }

    @Test
    @DisplayName("重置密码——用户不存在抛异常")
    void resetPassword_userNotFound() {
        when(staffMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> adminService.resetPassword(99L, "newpwd"));
    }

    @Test
    @DisplayName("重置密码——成功更新加密密码")
    void resetPassword_success() {
        Staff staff = new Staff();
        staff.setId(1L);
        staff.setPassword("oldHash");
        when(staffMapper.selectById(1L)).thenReturn(staff);
        when(staffMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> adminService.resetPassword(1L, "newpwd"));
        assertNotEquals("oldHash", staff.getPassword());
        verify(staffMapper).updateById(staff);
    }

    @Test
    @DisplayName("角色列表——返回全部角色")
    void roleList_success() {
        Role r1 = new Role(); r1.setId(1L); r1.setRoleName("admin");
        when(roleMapper.selectList(null)).thenReturn(List.of(r1));

        List<Role> roles = adminService.roleList();
        assertEquals(1, roles.size());
        assertEquals("admin", roles.get(0).getRoleName());
    }

    // ---- 排班管理 ----

    @Test
    @DisplayName("创建排班——插入排班和号源槽位")
    void createSchedule_success() {
        Schedule sch = new Schedule();
        ScheduleSlot slot = new ScheduleSlot();
        slot.setTotalQuota(10);
        when(scheduleMapper.insert(any())).thenReturn(1);
        when(scheduleSlotMapper.insert(any())).thenReturn(1);

        Schedule result = adminService.createSchedule(sch, List.of(slot));
        assertNotNull(result);
        assertEquals(10, slot.getRemaining()); // remaining = totalQuota
        verify(scheduleMapper).insert(sch);
        verify(scheduleSlotMapper).insert(slot);
    }

    @Test
    @DisplayName("停诊——排班不存在抛异常")
    void stopSchedule_notFound() {
        when(scheduleMapper.selectById(99L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> adminService.stopSchedule(99L, "请假"));
    }

    @Test
    @DisplayName("停诊——成功设置停诊标识")
    void stopSchedule_success() {
        Schedule sch = new Schedule();
        sch.setId(1L);
        sch.setIsStopped(0);
        when(scheduleMapper.selectById(1L)).thenReturn(sch);
        when(scheduleMapper.updateById(any())).thenReturn(1);

        assertDoesNotThrow(() -> adminService.stopSchedule(1L, "会议"));
        assertEquals(1, sch.getIsStopped());
        assertEquals("会议", sch.getStopReason());
    }
}
