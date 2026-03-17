package com.chp.security.service;

import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.security.dto.LoginVO;
import com.chp.security.dto.StaffLoginDTO;
import com.chp.security.dto.ResidentLoginDTO;
import com.chp.security.entity.Resident;
import com.chp.security.entity.Role;
import com.chp.security.entity.Staff;
import com.chp.security.mapper.admin.RoleMapper;
import com.chp.security.mapper.admin.StaffMapper;
import com.chp.security.mapper.resident.ResidentMapper;
import com.chp.security.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 单元测试")
class AuthServiceTest {

    @Mock private StaffMapper staffMapper;
    @Mock private ResidentMapper residentMapper;
    @Mock private RoleMapper roleMapper;
    @Mock private JwtUtils jwtUtils;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthService authService;

    private Staff mockStaff;
    private Role mockRole;
    private Resident mockResident;

    @BeforeEach
    void setUp() {
        mockStaff = new Staff();
        mockStaff.setId(1L);
        mockStaff.setUsername("AD001");
        mockStaff.setName("管理员");
        mockStaff.setPassword("$2a$10$encoded");
        mockStaff.setStatus(1);
        mockStaff.setRoleId(1L);
        mockStaff.setDeptCode("QKMZ");
        mockStaff.setDeptName("全科门诊");

        mockRole = new Role();
        mockRole.setId(1L);
        mockRole.setRoleCode("admin");
        mockRole.setRoleName("管理员");

        mockResident = new Resident();
        mockResident.setId(100L);
        mockResident.setPhone("13800138000");
        mockResident.setName("张三");
        mockResident.setPassword("$2a$10$encoded");
        mockResident.setStatus(1);
    }

    @Test
    @DisplayName("员工登录成功")
    void staffLogin_success() {
        when(staffMapper.selectOne(any())).thenReturn(mockStaff);
        when(passwordEncoder.matches("123456", "$2a$10$encoded")).thenReturn(true);
        when(roleMapper.selectById(1L)).thenReturn(mockRole);
        when(jwtUtils.generateAccessToken(anyLong(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(anyLong(), anyString())).thenReturn("refresh-token");
        when(jwtUtils.getAccessTokenExpiration()).thenReturn(3600L);

        StaffLoginDTO dto = new StaffLoginDTO();
        dto.setUsername("AD001");
        dto.setPassword("123456");

        LoginVO result = authService.staffLogin(dto);

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
        assertEquals(1L, result.getUserId());
        assertEquals("admin", result.getRole());
        assertEquals("admin", result.getDomain());
        verify(staffMapper).updateById(any(Staff.class)); // 更新最后登录时间
    }

    @Test
    @DisplayName("员工登录 - 用户不存在")
    void staffLogin_userNotFound() {
        when(staffMapper.selectOne(any())).thenReturn(null);

        StaffLoginDTO dto = new StaffLoginDTO();
        dto.setUsername("NONEXIST");
        dto.setPassword("123456");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.staffLogin(dto));
        assertEquals(StatusCode.UNAUTHORIZED, ex.getCode());
    }

    @Test
    @DisplayName("员工登录 - 密码错误")
    void staffLogin_wrongPassword() {
        when(staffMapper.selectOne(any())).thenReturn(mockStaff);
        when(passwordEncoder.matches("wrong", "$2a$10$encoded")).thenReturn(false);

        StaffLoginDTO dto = new StaffLoginDTO();
        dto.setUsername("AD001");
        dto.setPassword("wrong");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.staffLogin(dto));
        assertEquals(StatusCode.UNAUTHORIZED, ex.getCode());
    }

    @Test
    @DisplayName("员工登录 - 账号被禁用")
    void staffLogin_disabled() {
        mockStaff.setStatus(0);
        when(staffMapper.selectOne(any())).thenReturn(mockStaff);

        StaffLoginDTO dto = new StaffLoginDTO();
        dto.setUsername("AD001");
        dto.setPassword("123456");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.staffLogin(dto));
        assertEquals(StatusCode.ACCOUNT_DISABLED, ex.getCode());
    }

    @Test
    @DisplayName("居民登录成功")
    void residentLogin_success() {
        when(residentMapper.selectOne(any())).thenReturn(mockResident);
        when(passwordEncoder.matches("123456", "$2a$10$encoded")).thenReturn(true);
        when(jwtUtils.generateAccessToken(anyLong(), anyString(), anyString(), any(), anyString()))
                .thenReturn("res-access");
        when(jwtUtils.generateRefreshToken(anyLong(), anyString())).thenReturn("res-refresh");
        when(jwtUtils.getAccessTokenExpiration()).thenReturn(3600L);

        ResidentLoginDTO dto = new ResidentLoginDTO();
        dto.setPhone("13800138000");
        dto.setPassword("123456");

        LoginVO result = authService.residentLogin(dto);

        assertNotNull(result);
        assertEquals("resident", result.getRole());
        assertEquals("resident", result.getDomain());
        assertEquals(100L, result.getUserId());
    }

    @Test
    @DisplayName("连续失败5次后锁定")
    void staffLogin_lockAfter5Failures() {
        when(staffMapper.selectOne(any())).thenReturn(null);

        StaffLoginDTO dto = new StaffLoginDTO();
        dto.setUsername("LOCKTEST");
        dto.setPassword("bad");

        // 连续登录失败 5 次
        for (int i = 0; i < 5; i++) {
            assertThrows(BusinessException.class, () -> authService.staffLogin(dto));
        }

        // 第 6 次应该报 ACCOUNT_DISABLED（锁定）
        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.staffLogin(dto));
        assertEquals(StatusCode.ACCOUNT_DISABLED, ex.getCode());
        assertTrue(ex.getMessage().contains("30分钟"));
    }
}
