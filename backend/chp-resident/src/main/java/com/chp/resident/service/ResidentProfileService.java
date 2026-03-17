package com.chp.resident.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import com.chp.security.entity.Resident;
import com.chp.security.mapper.resident.ResidentMapper;
import com.chp.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResidentProfileService {

    private final ResidentMapper residentMapper;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    /** 获取个人信息（脱敏手机号） */
    public Map<String, Object> getProfile() {
        Resident r = getCurrentResident();
        Map<String, Object> info = new HashMap<>();
        info.put("id", r.getId());
        info.put("name", r.getName());
        info.put("phone", maskPhone(r.getPhone()));
        info.put("gender", r.getGender());
        info.put("birthDate", r.getBirthDate());
        info.put("idCard", maskIdCard(r.getIdCard()));
        info.put("bloodType", r.getBloodType());
        info.put("address", r.getAddress());
        info.put("avatar", r.getAvatar());
        return info;
    }

    /** 修改密码 */
    public void changePassword(String oldPassword, String newPassword) {
        Resident r = getCurrentResident();
        if (!passwordEncoder.matches(oldPassword, r.getPassword())) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "旧密码错误");
        }
        validatePasswordStrength(newPassword);
        r.setPassword(passwordEncoder.encode(newPassword));
        residentMapper.updateById(r);
    }

    /** 修改手机号 */
    public void changePhone(String password, String newPhone) {
        Resident r = getCurrentResident();
        if (!passwordEncoder.matches(password, r.getPassword())) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "密码验证失败");
        }
        // 检查新手机号是否已注册
        Long count = residentMapper.selectCount(
                new LambdaQueryWrapper<Resident>().eq(Resident::getPhone, newPhone));
        if (count > 0) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "该手机号已被注册");
        }
        r.setPhone(newPhone);
        residentMapper.updateById(r);
    }

    private Resident getCurrentResident() {
        Long userId = SecurityUtils.getCurrentUserId();
        Resident r = residentMapper.selectById(userId);
        if (r == null) {
            throw new BusinessException(StatusCode.UNAUTHORIZED, "用户不存在");
        }
        return r;
    }

    private void validatePasswordStrength(String pwd) {
        if (pwd == null || pwd.length() < 6 || pwd.length() > 20) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "密码长度需为6-20位");
        }
        boolean hasLetter = pwd.chars().anyMatch(Character::isLetter);
        boolean hasDigit = pwd.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BusinessException(StatusCode.BAD_REQUEST, "密码需同时包含字母和数字");
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) return idCard;
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }

    /** 获取紧急联系人（从 health_record 读取） */
    public Map<String, Object> getEmergencyContact() {
        Long userId = SecurityUtils.getCurrentUserId();
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            var rows = jdbcTemplate.queryForList(
                    "SELECT emergency_contact AS name, emergency_phone AS phone FROM health_record WHERE resident_id = ? LIMIT 1", userId);
            if (!rows.isEmpty()) {
                result.put("name", rows.get(0).get("name"));
                result.put("phone", rows.get(0).get("phone"));
                result.put("relation", "");
            }
        } catch (Exception ignored) {}
        return result;
    }

    /** 更新紧急联系人 */
    public void updateEmergencyContact(String name, String relation, String phone) {
        Long userId = SecurityUtils.getCurrentUserId();
        try {
            int updated = jdbcTemplate.update(
                    "UPDATE health_record SET emergency_contact = ?, emergency_phone = ? WHERE resident_id = ?",
                    name, phone, userId);
            if (updated == 0) {
                jdbcTemplate.update(
                        "INSERT INTO health_record (resident_id, emergency_contact, emergency_phone, allergy_history, past_medical_history) VALUES (?,?,?,'[]','[]')",
                        userId, name, phone);
            }
        } catch (Exception e) {
            throw new com.chp.common.exception.BusinessException(com.chp.common.constant.StatusCode.BAD_REQUEST, "更新联系人失败: " + e.getMessage());
        }
    }
}
