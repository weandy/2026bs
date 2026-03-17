package com.chp.common.aspect;

import com.chp.common.annotation.RequirePermission;
import com.chp.common.constant.StatusCode;
import com.chp.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * 模块级权限校验切面
 * 使用 Spring Security 原生 API，不依赖 chp-security 模块
 */
@Slf4j
@Aspect
@Component
public class RequirePermissionAspect {

    private static final Map<String, Set<String>> ROLE_PERMISSIONS = Map.of(
            "admin", Set.of("*:*"),
            "doctor", Set.of(
                    "workbench:*", "prescription:*", "followup:*",
                    "vaccination:view", "healthRecord:*",
                    "schedule:view", "publicHealth:*"
            ),
            "nurse", Set.of(
                    "workbench:view", "dispense:*",
                    "vaccination:*", "healthRecord:view",
                    "publicHealth:*", "schedule:view"
            ),
            "resident", Set.of(
                    "appointment:*", "vaccine:*",
                    "healthRecord:view", "visitRecord:view",
                    "message:view", "profile:*"
            )
    );

    @Around("@annotation(perm)")
    public Object checkPermission(ProceedingJoinPoint jp, RequirePermission perm) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException(StatusCode.UNAUTHORIZED, "请先登录");
        }

        // 从 authorities 中提取角色（格式：ROLE_xxx）
        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("");

        Set<String> allowed = ROLE_PERMISSIONS.getOrDefault(role, Set.of());

        if (allowed.contains("*:*")) {
            return jp.proceed();
        }

        String required = perm.module() + ":" + perm.op();
        String moduleWild = perm.module() + ":*";

        if (!allowed.contains(required) && !allowed.contains(moduleWild)) {
            log.warn("权限不足: role={}, required={}", role, required);
            throw new BusinessException(StatusCode.FORBIDDEN,
                    "权限不足，您的角色[" + role + "]不允许访问[" + perm.module() + "]模块");
        }

        return jp.proceed();
    }
}
