package com.chp.controller;

import com.chp.security.controller.AuthController;
import com.chp.security.dto.LoginVO;
import com.chp.security.dto.StaffLoginDTO;
import com.chp.security.dto.ResidentLoginDTO;
import com.chp.security.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 集成测试（MockMvc 独立模式）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController 集成测试")
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("POST /auth/admin/login 成功")
    void staffLogin_success() throws Exception {
        LoginVO loginVO = LoginVO.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .userId(1L)
                .role("admin")
                .domain("admin")
                .build();

        when(authService.staffLogin(any(StaffLoginDTO.class))).thenReturn(loginVO);

        StaffLoginDTO dto = new StaffLoginDTO();
        dto.setUsername("AD001");
        dto.setPassword("123456");

        mockMvc.perform(post("/auth/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("test-access-token"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.role").value("admin"));
    }

    @Test
    @DisplayName("POST /auth/resident/login 成功")
    void residentLogin_success() throws Exception {
        LoginVO loginVO = LoginVO.builder()
                .accessToken("res-access-token")
                .refreshToken("res-refresh-token")
                .userId(100L)
                .role("resident")
                .domain("resident")
                .build();

        when(authService.residentLogin(any(ResidentLoginDTO.class))).thenReturn(loginVO);

        ResidentLoginDTO dto = new ResidentLoginDTO();
        dto.setPhone("13800138000");
        dto.setPassword("123456");

        mockMvc.perform(post("/auth/resident/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("res-access-token"))
                .andExpect(jsonPath("$.data.role").value("resident"));
    }

    @Test
    @DisplayName("POST /auth/refresh 成功")
    void refreshToken_success() throws Exception {
        LoginVO loginVO = LoginVO.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .userId(1L)
                .role("admin")
                .build();

        when(authService.refreshToken("old-refresh-token")).thenReturn(loginVO);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"old-refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
    }

    @Test
    @DisplayName("POST /auth/logout 成功")
    void logout_success() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk());
    }
}
