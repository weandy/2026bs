package com.chp.common.annotation;

import java.lang.annotation.*;

/**
 * 审计日志注解，标注在 Controller 方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /** 日志类型：LOGIN/LOGOUT/RECORD_VIEW/RECORD_CREATE 等 */
    String type();

    /** 模块编码 */
    String module() default "";

    /** 操作描述 */
    String description() default "";
}
