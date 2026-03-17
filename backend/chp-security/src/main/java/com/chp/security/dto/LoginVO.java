package com.chp.security.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应
 */
@Data
@Builder
public class LoginVO {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private Long userId;
    private String name;
    private String role;
    private String domain;
    private String deptCode;
    private String deptName;
}
