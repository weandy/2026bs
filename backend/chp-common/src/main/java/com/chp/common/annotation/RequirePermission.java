package com.chp.common.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /** 模块编码 */
    String module();

    /** 操作类型：view/create/edit/delete/export */
    String op();
}
