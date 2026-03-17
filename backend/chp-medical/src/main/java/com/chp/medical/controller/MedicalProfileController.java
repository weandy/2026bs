package com.chp.medical.controller;

import com.chp.common.result.Result;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 医护人员个人中心接口
 */
@Slf4j
@RestController
@RequestMapping("/medical/profile")
@RequiredArgsConstructor
public class MedicalProfileController {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    /** GET /medical/profile — 获取当前用户个人简介 */
    @GetMapping
    public Result<Map<String, Object>> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        try {
            // 确保记录存在
            jdbcTemplate.update(
                "INSERT IGNORE INTO staff_profile (id) VALUES (?)", userId);
            var row = jdbcTemplate.queryForList(
                "SELECT sp.specialty, sp.bio, sp.title, sp.years_exp, su.name, su.phone " +
                "FROM staff_profile sp " +
                "JOIN sys_user su ON su.id = sp.id " +
                "WHERE sp.id = ?", userId);
            return Result.success(row.isEmpty() ? new HashMap<>() : row.get(0));
        } catch (Exception e) {
            log.warn("获取简介失败", e);
            return Result.success(Collections.emptyMap());
        }
    }

    /** PUT /medical/profile — 更新简介 */
    @PutMapping
    public Result<Void> updateProfile(@RequestBody Map<String, Object> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        try {
            jdbcTemplate.update(
                "INSERT INTO staff_profile (id, specialty, bio, title, years_exp) VALUES (?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE specialty=VALUES(specialty), bio=VALUES(bio), title=VALUES(title), years_exp=VALUES(years_exp)",
                userId,
                body.getOrDefault("specialty", ""),
                body.getOrDefault("bio", ""),
                body.getOrDefault("title", ""),
                body.getOrDefault("yearsExp", 0)
            );
            return Result.success(null);
        } catch (Exception e) {
            log.error("更新简介失败", e);
            return Result.error(400, "更新失败");
        }
    }

    /** PUT /medical/profile/password — 修改密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        String oldPwd = body.get("oldPassword");
        String newPwd = body.get("newPassword");
        try {
            String storedHash = jdbcTemplate.queryForObject(
                "SELECT password FROM sys_user WHERE id = ?", String.class, userId);
            if (!passwordEncoder.matches(oldPwd, storedHash)) {
                return Result.error(400, "原密码不正确");
            }
            jdbcTemplate.update("UPDATE sys_user SET password = ? WHERE id = ?",
                passwordEncoder.encode(newPwd), userId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error(400, "修改失败");
        }
    }

    /** PUT /medical/profile/phone — 修改手机号 */
    @PutMapping("/phone")
    public Result<Void> changePhone(@RequestBody Map<String, String> body) {
        Long userId = SecurityUtils.getCurrentUserId();
        String phone = body.get("phone");
        try {
            jdbcTemplate.update("UPDATE sys_user SET phone = ? WHERE id = ?", phone, userId);
            return Result.success(null);
        } catch (Exception e) {
            log.error("修改手机号失败", e);
            return Result.error(400, "修改失败");
        }
    }

    /** GET /medical/profile/public/{doctorId} — 居民端公开查看医生信息 */
    @GetMapping("/public/{doctorId}")
    public Result<Map<String, Object>> publicProfile(@PathVariable Long doctorId) {
        try {
            var row = jdbcTemplate.queryForList(
                "SELECT su.name, su.dept_name, sp.specialty, sp.bio, sp.title, sp.years_exp " +
                "FROM sys_user su " +
                "LEFT JOIN staff_profile sp ON sp.id = su.id " +
                "WHERE su.id = ?", doctorId);
            return Result.success(row.isEmpty() ? Collections.emptyMap() : row.get(0));
        } catch (Exception e) {
            log.warn("获取医生公开信息失败", e);
            return Result.success(Collections.emptyMap());
        }
    }
}
