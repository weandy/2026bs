package com.chp.security.controller;

import com.chp.common.result.Result;
import com.chp.security.dto.LoginVO;
import com.chp.security.dto.ResidentLoginDTO;
import com.chp.security.dto.StaffLoginDTO;
import com.chp.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证接口（公开，无需 Token）
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 医护/管理员登录
     * POST /api/auth/admin/login
     */
    @PostMapping("/admin/login")
    public Result<LoginVO> adminLogin(@Valid @RequestBody StaffLoginDTO dto) {
        return Result.success(authService.staffLogin(dto));
    }

    /**
     * 居民登录
     * POST /api/auth/resident/login
     */
    @PostMapping("/resident/login")
    public Result<LoginVO> residentLogin(@Valid @RequestBody ResidentLoginDTO dto) {
        return Result.success(authService.residentLogin(dto));
    }

    /**
     * Token 刷新
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        return Result.success(authService.refreshToken(refreshToken));
    }

    /**
     * 退出 — 将 Refresh Token 加入黑名单
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody(required = false) Map<String, String> body) {
        if (body != null && body.containsKey("refreshToken")) {
            authService.invalidateRefreshToken(body.get("refreshToken"));
        }
        return Result.success("退出成功", null);
    }
}
