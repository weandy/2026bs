package com.chp.common.constant;

/**
 * 业务状态码常量
 */
public final class StatusCode {

    private StatusCode() {}

    /** 成功 */
    public static final int SUCCESS = 200;
    /** 参数校验失败 */
    public static final int BAD_REQUEST = 400;
    /** 未登录/Token 过期 */
    public static final int UNAUTHORIZED = 401;
    /** 无权限 */
    public static final int FORBIDDEN = 403;
    /** 资源不存在 */
    public static final int NOT_FOUND = 404;
    /** 服务器异常 */
    public static final int SERVER_ERROR = 500;

    // ---- 业务错误码 ----
    /** 号源已约满 */
    public static final int SLOT_FULL = 10001;
    /** 重复预约 */
    public static final int DUPLICATE_APPOINTMENT = 10002;
    /** 药品库存不足 */
    public static final int DRUG_STOCK_INSUFFICIENT = 10003;
    /** 处方已发药，不可撤销 */
    public static final int PRESCRIPTION_DISPENSED = 10004;
    /** 账号已被禁用 */
    public static final int ACCOUNT_DISABLED = 10005;
    /** 验证码错误 */
    public static final int CAPTCHA_ERROR = 10006;
}
