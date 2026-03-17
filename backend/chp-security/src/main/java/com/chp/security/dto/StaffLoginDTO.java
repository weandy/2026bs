package com.chp.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 医护/管理员登录请求
 */
@Data
public class StaffLoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
}
