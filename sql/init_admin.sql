-- ============================================================
-- 医护/管理域数据库初始化脚本
-- 数据库：chp_admin
-- ============================================================

CREATE DATABASE IF NOT EXISTS chp_admin
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE chp_admin;

-- ------------------------------------------------------------
-- 医护/管理员账号表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS staff (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    username        VARCHAR(30)     NOT NULL                    COMMENT '登录用户名（工号）',
    name            VARCHAR(20)     NOT NULL                    COMMENT '真实姓名',
    password        VARCHAR(128)    NOT NULL                    COMMENT 'BCrypt加密',
    phone           VARCHAR(11)     NOT NULL,
    gender          TINYINT(1)      NOT NULL DEFAULT 1,
    dept_code       VARCHAR(20)     NULL                        COMMENT '所属科室',
    dept_name       VARCHAR(50)     NULL                        COMMENT '科室名称（冗余）',
    job_title       VARCHAR(30)     NULL                        COMMENT '职称',
    introduction    VARCHAR(200)    NULL                        COMMENT '医生简介',
    role_id         BIGINT          NOT NULL                    COMMENT '角色ID',
    status          TINYINT(1)      NOT NULL DEFAULT 1          COMMENT '1正常 0禁用',
    last_login_at   DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_dept_code (dept_code),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医护/管理员账号';

-- ------------------------------------------------------------
-- 角色表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS role (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    role_code       VARCHAR(30)     NOT NULL                    COMMENT '角色编码',
    role_name       VARCHAR(50)     NOT NULL                    COMMENT '角色名称',
    description     VARCHAR(200)    NULL,
    status          TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色定义';

-- 初始角色数据
INSERT INTO role (role_code, role_name, description) VALUES
('resident', '居民', '居民端用户'),
('doctor',   '医生', '具有处方权的医护人员'),
('nurse',    '护士', '护士/药师，无处方权'),
('admin',    '系统管理员', '后台管理全权限');

-- ------------------------------------------------------------
-- 功能模块表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS module_permission (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    module_code     VARCHAR(50)     NOT NULL                    COMMENT '模块编码',
    module_name     VARCHAR(100)    NOT NULL                    COMMENT '模块名称',
    description     VARCHAR(200)    NULL,
    sort_order      INT             NOT NULL DEFAULT 0,
    status          TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_module_code (module_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='功能模块注册表';

-- 初始模块数据
INSERT INTO module_permission (module_code, module_name, sort_order) VALUES
('appointment',    '预约挂号',         10),
('health_record',  '健康档案',         20),
('health_export',  '便捷就诊导出',     30),
('vaccine_appt',   '疫苗预约',         40),
('visit_record',   '就诊记录',         50),
('message',        '消息通知',         60),
('consultation',   '接诊工作台',       70),
('prescription',   '电子处方',         80),
('pharmacy',       '药房发药核对',     90),
('follow_up',      '慢病随访',         100),
('vaccine_admin',  '接种管理',         110),
('public_health',  '公卫服务',         120),
('lab_order',      '检验检查',         130),
('schedule_view',  '排班查看',         140),
('user_mgmt',      '用户权限管理',     150),
('schedule_mgmt',  '排班号源管理',     160),
('drug_mgmt',      '药品库存',         170),
('vaccine_stock',  '疫苗库存',         180),
('dict_mgmt',      '数据字典',         190),
('report',         '统计报表',         200),
('audit_log',      '操作日志',         210),
('sys_config',     '系统配置',         220),
('public_screen',  '候诊公屏',         230);

-- ------------------------------------------------------------
-- 角色-模块权限关联表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS role_module_perm (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    role_id         BIGINT          NOT NULL,
    module_code     VARCHAR(50)     NOT NULL,
    can_view        TINYINT(1)      NOT NULL DEFAULT 0,
    can_create      TINYINT(1)      NOT NULL DEFAULT 0,
    can_edit        TINYINT(1)      NOT NULL DEFAULT 0,
    can_delete      TINYINT(1)      NOT NULL DEFAULT 0,
    can_export      TINYINT(1)      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_module (role_id, module_code),
    KEY idx_module_code (module_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色模块权限关联';

-- ------------------------------------------------------------
-- 科室字典表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS dept (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    dept_code       VARCHAR(20)     NOT NULL                    COMMENT '科室编码',
    dept_name       VARCHAR(50)     NOT NULL,
    is_appt_open    TINYINT(1)      NOT NULL DEFAULT 1          COMMENT '是否开放预约',
    sort_order      INT             NOT NULL DEFAULT 0,
    status          TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dept_code (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室字典';

-- 初始科室数据
INSERT INTO dept (dept_code, dept_name, is_appt_open, sort_order) VALUES
('QKMZ', '全科门诊', 1, 10),
('KQK',  '口腔科',   1, 20),
('FYBJ', '妇幼保健科', 1, 30),
('GGWS', '公共卫生科', 0, 40);

-- ------------------------------------------------------------
-- 排班主表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS schedule (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    staff_id        BIGINT          NOT NULL                    COMMENT '出诊医生ID',
    staff_name      VARCHAR(20)     NOT NULL,
    dept_code       VARCHAR(20)     NOT NULL,
    dept_name       VARCHAR(50)     NOT NULL,
    schedule_date   DATE            NOT NULL                    COMMENT '出诊日期',
    is_stopped      TINYINT(1)      NOT NULL DEFAULT 0          COMMENT '是否停诊',
    stop_reason     VARCHAR(200)    NULL,
    created_by      BIGINT          NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_staff_date (staff_id, schedule_date),
    KEY idx_dept_date (dept_code, schedule_date),
    KEY idx_schedule_date (schedule_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班主表';

-- ------------------------------------------------------------
-- 出诊时段表（号源管理，乐观锁）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS schedule_slot (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    schedule_id     BIGINT          NOT NULL,
    time_period     TINYINT(1)      NOT NULL                    COMMENT '1上午 2下午',
    total_quota     INT             NOT NULL DEFAULT 20         COMMENT '总号源数',
    remaining       INT             NOT NULL                    COMMENT '剩余号源',
    version         INT             NOT NULL DEFAULT 0          COMMENT '乐观锁版本号',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_schedule_period (schedule_id, time_period),
    KEY idx_remaining (remaining)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出诊时段号源（乐观锁）';

-- ------------------------------------------------------------
-- 药品字典表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS drug (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    drug_code       VARCHAR(30)     NOT NULL,
    generic_name    VARCHAR(100)    NOT NULL                    COMMENT '通用名',
    trade_name      VARCHAR(100)    NULL                        COMMENT '商品名',
    spec            VARCHAR(100)    NOT NULL                    COMMENT '规格',
    dosage_form     VARCHAR(30)     NOT NULL                    COMMENT '剂型',
    unit            VARCHAR(20)     NOT NULL                    COMMENT '单位',
    pinyin_code     VARCHAR(20)     NULL                        COMMENT '拼音首字母',
    manufacturer    VARCHAR(100)    NULL                        COMMENT '生产厂家',
    alert_qty       INT             NOT NULL DEFAULT 20         COMMENT '库存预警阈值',
    status          TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_drug_code (drug_code),
    KEY idx_generic_name (generic_name),
    KEY idx_pinyin_code (pinyin_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品字典';

-- ------------------------------------------------------------
-- 药品库存表（按批次）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS drug_stock (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    drug_id         BIGINT          NOT NULL,
    batch_no        VARCHAR(50)     NOT NULL,
    quantity        INT             NOT NULL                    COMMENT '当前库存数量',
    expire_date     DATE            NOT NULL                    COMMENT '有效期至',
    supplier        VARCHAR(100)    NULL,
    purchase_price  DECIMAL(10,2)   NULL,
    inbound_at      DATE            NOT NULL                    COMMENT '入库日期',
    inbound_by      BIGINT          NOT NULL,
    status          TINYINT(1)      NOT NULL DEFAULT 1          COMMENT '1正常 2已清零 3已报废',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_drug_id (drug_id),
    KEY idx_expire_date (expire_date),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品库存（按批次）';

-- ------------------------------------------------------------
-- 药品出入库流水表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS drug_stock_log (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    drug_id         BIGINT          NOT NULL,
    drug_name       VARCHAR(100)    NOT NULL                    COMMENT '冗余快照',
    batch_no        VARCHAR(50)     NOT NULL,
    op_type         TINYINT(1)      NOT NULL                    COMMENT '1入库 2出库 3盘点 4报废',
    quantity        INT             NOT NULL,
    direction       TINYINT(1)      NOT NULL                    COMMENT '1增加 2减少',
    balance         INT             NOT NULL                    COMMENT '操作后余量',
    related_id      BIGINT          NULL,
    operator_id     BIGINT          NOT NULL,
    operator_name   VARCHAR(20)     NOT NULL,
    op_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark          VARCHAR(200)    NULL,
    PRIMARY KEY (id),
    KEY idx_drug_id (drug_id),
    KEY idx_op_time (op_time),
    KEY idx_op_type (op_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品出入库流水';

-- ------------------------------------------------------------
-- 疫苗字典表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vaccine (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    vaccine_code    VARCHAR(30)     NOT NULL,
    vaccine_name    VARCHAR(100)    NOT NULL,
    target_disease  VARCHAR(100)    NULL,
    applicable_age  VARCHAR(50)     NULL,
    total_doses     TINYINT         NOT NULL DEFAULT 1,
    dose_interval   VARCHAR(100)    NULL,
    storage_req     VARCHAR(50)     NULL,
    vaccine_type    TINYINT(1)      NOT NULL DEFAULT 2          COMMENT '1免疫规划 2自费',
    price           DECIMAL(10,2)   NULL,
    alert_qty       INT             NOT NULL DEFAULT 10,
    status          TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vaccine_code (vaccine_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗字典';

-- ------------------------------------------------------------
-- 疫苗库存表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vaccine_stock (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    vaccine_id      BIGINT          NOT NULL,
    batch_no        VARCHAR(50)     NOT NULL,
    quantity        INT             NOT NULL,
    expire_date     DATE            NOT NULL,
    inbound_at      DATE            NOT NULL,
    inbound_by      BIGINT          NOT NULL,
    status          TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_vaccine_id (vaccine_id),
    KEY idx_expire_date (expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗库存（按批次）';

-- ------------------------------------------------------------
-- 疫苗出入库流水表（V2.0新增）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vaccine_stock_log (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    vaccine_id      BIGINT      NOT NULL,
    vaccine_name    VARCHAR(100) NOT NULL                       COMMENT '冗余快照',
    batch_no        VARCHAR(50) NOT NULL,
    op_type         TINYINT(1)  NOT NULL                        COMMENT '1入库 2出库(接种) 3盘点 4报废',
    quantity        INT         NOT NULL,
    direction       TINYINT(1)  NOT NULL                        COMMENT '1增加 2减少',
    balance         INT         NOT NULL                        COMMENT '操作后余量',
    related_id      BIGINT      NULL,
    operator_id     BIGINT      NOT NULL,
    operator_name   VARCHAR(20) NOT NULL,
    op_time         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark          VARCHAR(200) NULL,
    PRIMARY KEY (id),
    KEY idx_vaccine_id (vaccine_id),
    KEY idx_op_time (op_time),
    KEY idx_op_type (op_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗出入库流水';

-- ------------------------------------------------------------
-- 慢病随访计划表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS follow_up_plan (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    resident_id     BIGINT          NOT NULL,
    resident_name   VARCHAR(20)     NOT NULL,
    chronic_type    VARCHAR(30)     NOT NULL,
    doctor_id       BIGINT          NOT NULL,
    doctor_name     VARCHAR(20)     NOT NULL,
    frequency       TINYINT(1)      NOT NULL                    COMMENT '1每月 2每季度 3每半年',
    follow_up_method TINYINT(1)     NOT NULL DEFAULT 1          COMMENT '1电话 2门诊 3上门',
    next_follow_date DATE           NOT NULL,
    status          TINYINT(1)      NOT NULL DEFAULT 1          COMMENT '1管理中 2已停止',
    created_by      BIGINT          NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    KEY idx_resident_id (resident_id),
    KEY idx_doctor_id (doctor_id),
    KEY idx_next_date (next_follow_date),
    KEY idx_chronic_type (chronic_type),
    KEY idx_resident_chronic (resident_id, chronic_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='慢病随访计划';

-- ------------------------------------------------------------
-- 慢病随访记录表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS follow_up_record (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    plan_id         BIGINT          NOT NULL,
    resident_id     BIGINT          NOT NULL,
    follow_date     DATE            NOT NULL,
    follow_method   TINYINT(1)      NOT NULL,
    systolic_bp     SMALLINT        NULL                        COMMENT '收缩压',
    diastolic_bp    SMALLINT        NULL                        COMMENT '舒张压',
    fasting_glucose DECIMAL(5,2)    NULL                        COMMENT '空腹血糖',
    postprandial_glucose DECIMAL(5,2) NULL                      COMMENT '餐后2h血糖',
    medication_compliance TINYINT(1) NULL                       COMMENT '1规律 2间断 3不服药',
    lifestyle_issues VARCHAR(200)   NULL,
    health_guidance TEXT            NULL,
    next_follow_date DATE           NULL,
    staff_id        BIGINT          NOT NULL,
    staff_name      VARCHAR(20)     NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_plan_id (plan_id),
    KEY idx_resident_id (resident_id),
    KEY idx_follow_date (follow_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='慢病随访记录';

-- ------------------------------------------------------------
-- 公卫服务记录表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public_health_record (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    resident_id     BIGINT          NOT NULL,
    resident_name   VARCHAR(20)     NOT NULL,
    service_type    VARCHAR(30)     NOT NULL                    COMMENT 'elder_exam/prenatal/child_care',
    service_date    DATE            NOT NULL,
    indicators      JSON            NOT NULL,
    conclusion      TINYINT(1)      NULL                        COMMENT '1正常 2异常',
    conclusion_desc VARCHAR(500)    NULL,
    referral_needed TINYINT(1)      NOT NULL DEFAULT 0,
    referral_reason VARCHAR(200)    NULL,
    staff_id        BIGINT          NOT NULL,
    staff_name      VARCHAR(20)     NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    KEY idx_resident_service (resident_id, service_type),
    KEY idx_service_date (service_date),
    KEY idx_service_type (service_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公卫服务记录';

-- ------------------------------------------------------------
-- ICD-10 疾病码表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS icd_code (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    code            VARCHAR(10)     NOT NULL,
    name_cn         VARCHAR(200)    NOT NULL,
    name_en         VARCHAR(200)    NULL,
    category        VARCHAR(100)    NULL,
    pinyin_code     VARCHAR(20)     NULL,
    status          TINYINT(1)      NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_name_cn (name_cn),
    KEY idx_pinyin_code (pinyin_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ICD-10疾病编码';

-- ------------------------------------------------------------
-- 操作审计日志表（只追加）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_log (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    operator_id     BIGINT          NOT NULL,
    operator_name   VARCHAR(20)     NOT NULL,
    operator_role   VARCHAR(20)     NOT NULL,
    log_type        VARCHAR(30)     NOT NULL,
    module_code     VARCHAR(50)     NULL,
    action          VARCHAR(50)     NOT NULL,
    target_id       BIGINT          NULL,
    target_desc     VARCHAR(200)    NULL,
    request_ip      VARCHAR(50)     NULL,
    request_ua      VARCHAR(500)    NULL,
    result          TINYINT(1)      NOT NULL DEFAULT 1          COMMENT '1成功 0失败',
    fail_reason     VARCHAR(200)    NULL,
    op_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_operator_id (operator_id),
    KEY idx_log_type (log_type),
    KEY idx_op_time (op_time),
    KEY idx_module_code (module_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志（只追加）';

-- ------------------------------------------------------------
-- 系统配置表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_config (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    config_key      VARCHAR(50)     NOT NULL,
    config_value    VARCHAR(500)    NOT NULL,
    config_name     VARCHAR(100)    NOT NULL,
    config_group    VARCHAR(30)     NULL,
    is_editable     TINYINT(1)      NOT NULL DEFAULT 1,
    updated_by      BIGINT          NULL,
    updated_at      DATETIME        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

-- 初始配置
INSERT INTO sys_config (config_key, config_value, config_name, config_group) VALUES
('appt.max_days_ahead',      '7',    '预约可提前天数',       'appointment'),
('appt.default_morning_quota','20',  '上午默认号源数',       'appointment'),
('appt.default_afternoon_quota','15','下午默认号源数',       'appointment'),
('drug.alert_expire_days',   '30',   '近效期预警天数',       'drug'),
('drug.default_alert_qty',   '20',   '库存预警默认阈值',     'drug'),
('vaccine.alert_expire_days','30',   '疫苗近效期预警天数',   'vaccine'),
('follow_up.overdue_alert',  '7',    '随访超期预警天数',     'follow_up'),
('screen.refresh_interval',  '5',    '公屏刷新间隔（秒）',  'system'),
('pwd.min_length',        '6',  '密码最小长度',       'security'),
('pwd.max_length',        '20', '密码最大长度',       'security'),
('login.max_fail_count',  '5',  '最大登录失败次数',   'security'),
('login.lock_minutes',    '30', '登录锁定分钟数',     'security');

-- ------------------------------------------------------------
-- 公告通知表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notice (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    title           VARCHAR(100)    NOT NULL,
    content         TEXT            NOT NULL,
    notice_type     VARCHAR(20)     NOT NULL DEFAULT 'general'  COMMENT 'general/stop_work/holiday',
    is_top          TINYINT(1)      NOT NULL DEFAULT 0,
    effective_from  DATE            NULL,
    effective_to    DATE            NULL,
    publisher_id    BIGINT          NOT NULL,
    publisher_name  VARCHAR(20)     NOT NULL,
    status          TINYINT(1)      NOT NULL DEFAULT 1          COMMENT '1已发布 0草稿 2已撤回',
    published_at    DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_status_top (status, is_top),
    KEY idx_effective (effective_from, effective_to)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告通知';

-- ------------------------------------------------------------
-- 调班申请表（V2.0新增）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS schedule_transfer_request (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    schedule_id     BIGINT      NOT NULL                        COMMENT '原排班ID',
    staff_id        BIGINT      NOT NULL                        COMMENT '申请人ID',
    staff_name      VARCHAR(20) NOT NULL                        COMMENT '申请人姓名',
    request_reason  VARCHAR(200) NOT NULL                       COMMENT '调班原因',
    status          TINYINT(1)  NOT NULL DEFAULT 0              COMMENT '0待审批 1通过 2驳回',
    approver_id     BIGINT      NULL,
    approver_name   VARCHAR(20) NULL,
    reject_reason   VARCHAR(200) NULL,
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_staff_id (staff_id),
    KEY idx_schedule_id (schedule_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调班申请';
