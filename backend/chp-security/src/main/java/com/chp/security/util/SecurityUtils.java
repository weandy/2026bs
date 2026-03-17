package com.chp.security.util;

import com.chp.security.filter.JwtUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * 获取当前登录用户信息
     */
    public static JwtUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
            return (JwtUserDetails) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        JwtUserDetails user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentRole() {
        JwtUserDetails user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }

    /**
     * 获取当前用户域
     */
    public static String getCurrentDomain() {
        JwtUserDetails user = getCurrentUser();
        return user != null ? user.getDomain() : null;
    }
}
