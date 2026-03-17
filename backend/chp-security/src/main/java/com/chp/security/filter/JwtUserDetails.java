package com.chp.security.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * JWT 中的用户信息
 */
@Getter
@AllArgsConstructor
public class JwtUserDetails {

    private Long userId;
    private String name;
    private String role;
    private String deptCode;
    private String domain;
}
