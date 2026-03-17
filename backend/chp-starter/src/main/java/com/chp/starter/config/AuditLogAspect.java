package com.chp.starter.config;

import com.chp.admin.entity.AuditLog;
import com.chp.admin.mapper.AuditLogMapper;
import com.chp.security.filter.JwtUserDetails;
import com.chp.security.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 审计日志 AOP 切面
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);
    private final AuditLogMapper auditLogMapper;

    public AuditLogAspect(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint pjp, com.chp.common.annotation.AuditLog auditLog) throws Throwable {
        Object result = pjp.proceed();
        try {
            JwtUserDetails user = SecurityUtils.getCurrentUser();
            HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

            AuditLog entity = new AuditLog();
            entity.setOperatorId(user != null ? user.getUserId() : 0L);
            entity.setOperatorName(user != null ? user.getName() : "anonymous");
            entity.setOperatorRole(user != null ? user.getRole() : "unknown");
            entity.setLogType(auditLog.type());
            entity.setModuleCode(auditLog.module());
            entity.setAction(auditLog.description().isEmpty() ? pjp.getSignature().getName() : auditLog.description());
            entity.setRequestIp(getClientIp(req));
            entity.setOpTime(LocalDateTime.now());
            entity.setResult(1);

            auditLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("审计日志写入失败: {}", e.getMessage());
        }
        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip.contains(",") ? ip.split(",")[0].trim() : ip;
    }
}
