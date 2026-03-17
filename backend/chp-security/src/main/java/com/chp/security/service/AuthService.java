package com.chp.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.security.dto.LoginVO;
import com.chp.security.dto.ResidentLoginDTO;
import com.chp.security.dto.StaffLoginDTO;
import com.chp.security.entity.Resident;
import com.chp.security.entity.Role;
import com.chp.security.entity.Staff;
import com.chp.security.mapper.admin.RoleMapper;
import com.chp.security.mapper.admin.StaffMapper;
import com.chp.security.mapper.resident.ResidentMapper;
import com.chp.security.util.JwtUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final StaffMapper staffMapper;
    private final ResidentMapper residentMapper;
    private final RoleMapper roleMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    /** 登录失败计数缓存 key=username/phone, value=失败次数 */
    private final Cache<String, Integer> loginFailCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    /** Refresh Token 黑名单（logout 后加入，过期自动清理） */
    private final Cache<String, Boolean> refreshTokenBlacklist = Caffeine.newBuilder()
            .expireAfterWrite(7, TimeUnit.DAYS)
            .maximumSize(10000)
            .build();

    private static final int MAX_FAIL_COUNT = 5;

    /**
     * 医护/管理员登录
     */
    public LoginVO staffLogin(StaffLoginDTO dto) {
        checkLocked(dto.getUsername());

        Staff staff = staffMapper.selectOne(
                new LambdaQueryWrapper<Staff>().eq(Staff::getUsername, dto.getUsername()));
        if (staff == null) {
            recordFail(dto.getUsername());
            throw new BusinessException(StatusCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (staff.getStatus() == 0) {
            throw new BusinessException(StatusCode.ACCOUNT_DISABLED, "账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), staff.getPassword())) {
            recordFail(dto.getUsername());
            throw new BusinessException(StatusCode.UNAUTHORIZED, "用户名或密码错误");
        }

        // 查询角色
        Role role = roleMapper.selectById(staff.getRoleId());
        String roleCode = role != null ? role.getRoleCode() : "unknown";

        // 清除失败计数
        loginFailCache.invalidate(dto.getUsername());

        // 更新最后登录时间
        staff.setLastLoginAt(LocalDateTime.now());
        staffMapper.updateById(staff);

        // 生成 Token
        String accessToken = jwtUtils.generateAccessToken(
                staff.getId(), staff.getName(), roleCode, staff.getDeptCode(), "admin");
        String refreshToken = jwtUtils.generateRefreshToken(staff.getId(), "admin");

        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getAccessTokenExpiration())
                .userId(staff.getId())
                .name(staff.getName())
                .role(roleCode)
                .domain("admin")
                .deptCode(staff.getDeptCode())
                .deptName(staff.getDeptName())
                .build();
    }

    /**
     * 居民登录
     */
    public LoginVO residentLogin(ResidentLoginDTO dto) {
        checkLocked(dto.getPhone());

        Resident resident = residentMapper.selectOne(
                new LambdaQueryWrapper<Resident>().eq(Resident::getPhone, dto.getPhone()));
        if (resident == null) {
            recordFail(dto.getPhone());
            throw new BusinessException(StatusCode.UNAUTHORIZED, "手机号或密码错误");
        }
        if (resident.getStatus() == 0) {
            throw new BusinessException(StatusCode.ACCOUNT_DISABLED, "账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), resident.getPassword())) {
            recordFail(dto.getPhone());
            throw new BusinessException(StatusCode.UNAUTHORIZED, "手机号或密码错误");
        }

        loginFailCache.invalidate(dto.getPhone());

        resident.setLastLoginAt(LocalDateTime.now());
        residentMapper.updateById(resident);

        String accessToken = jwtUtils.generateAccessToken(
                resident.getId(), resident.getName(), "resident", null, "resident");
        String refreshToken = jwtUtils.generateRefreshToken(resident.getId(), "resident");

        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getAccessTokenExpiration())
                .userId(resident.getId())
                .name(resident.getName())
                .role("resident")
                .domain("resident")
                .build();
    }

    /**
     * Token 刷新
     */
    public LoginVO refreshToken(String refreshTokenStr) {
        if (!jwtUtils.validateToken(refreshTokenStr)) {
            throw new BusinessException(StatusCode.UNAUTHORIZED, "Refresh Token 无效或已过期");
        }

        // 检查黑名单
        if (Boolean.TRUE.equals(refreshTokenBlacklist.getIfPresent(refreshTokenStr))) {
            throw new BusinessException(StatusCode.UNAUTHORIZED, "该 Token 已被注销");
        }

        String type = jwtUtils.parseToken(refreshTokenStr).get("type", String.class);
        if (!"refresh".equals(type)) {
            throw new BusinessException(StatusCode.UNAUTHORIZED, "无效的 Token 类型");
        }

        Long userId = jwtUtils.getUserId(refreshTokenStr);
        String domain = jwtUtils.getDomain(refreshTokenStr);

        if ("admin".equals(domain)) {
            Staff staff = staffMapper.selectById(userId);
            if (staff == null || staff.getStatus() == 0) {
                throw new BusinessException(StatusCode.UNAUTHORIZED, "账号不存在或已禁用");
            }
            Role role = roleMapper.selectById(staff.getRoleId());
            String roleCode = role != null ? role.getRoleCode() : "unknown";
            String newAccessToken = jwtUtils.generateAccessToken(
                    staff.getId(), staff.getName(), roleCode, staff.getDeptCode(), "admin");
            String newRefreshToken = jwtUtils.generateRefreshToken(staff.getId(), "admin");
            return LoginVO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(jwtUtils.getAccessTokenExpiration())
                    .userId(staff.getId())
                    .name(staff.getName())
                    .role(roleCode)
                    .domain("admin")
                    .deptCode(staff.getDeptCode())
                    .deptName(staff.getDeptName())
                    .build();
        } else {
            Resident resident = residentMapper.selectById(userId);
            if (resident == null || resident.getStatus() == 0) {
                throw new BusinessException(StatusCode.UNAUTHORIZED, "账号不存在或已禁用");
            }
            String newAccessToken = jwtUtils.generateAccessToken(
                    resident.getId(), resident.getName(), "resident", null, "resident");
            String newRefreshToken = jwtUtils.generateRefreshToken(resident.getId(), "resident");
            return LoginVO.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(jwtUtils.getAccessTokenExpiration())
                    .userId(resident.getId())
                    .name(resident.getName())
                    .role("resident")
                    .domain("resident")
                    .build();
        }
    }

    /**
     * 注销 Refresh Token（加入黑名单）
     */
    public void invalidateRefreshToken(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenBlacklist.put(refreshToken, Boolean.TRUE);
            log.info("Refresh token invalidated on logout");
        }
    }

    private void checkLocked(String key) {
        Integer count = loginFailCache.getIfPresent(key);
        if (count != null && count >= MAX_FAIL_COUNT) {
            throw new BusinessException(StatusCode.ACCOUNT_DISABLED, "登录失败次数过多，请30分钟后重试");
        }
    }

    private void recordFail(String key) {
        Integer count = loginFailCache.getIfPresent(key);
        loginFailCache.put(key, (count == null ? 0 : count) + 1);
    }
}
