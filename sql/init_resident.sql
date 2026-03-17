-- ============================================================
-- 居民域数据库初始化脚本
-- 数据库：chp_resident
-- ============================================================

CREATE DATABASE IF NOT EXISTS chp_resident
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE chp_resident;

-- ------------------------------------------------------------
-- 居民账号与基本信息表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS resident (
    id              BIGINT          NOT NULL AUTO_INCREMENT     COMMENT '主键',
    id_card         VARCHAR(18)     NOT NULL                    COMMENT '身份证号（唯一标识，加密存储）',
    name            VARCHAR(20)     NOT NULL                    COMMENT '姓名',
    gender          TINYINT(1)      NOT NULL DEFAULT 1          COMMENT '性别：1男 2女',
    birth_date      DATE            NOT NULL                    COMMENT '出生日期',
    phone           VARCHAR(11)     NOT NULL                    COMMENT '手机号（登录名）',
    password        VARCHAR(128)    NOT NULL                    COMMENT '密码（BCrypt加密）',
    blood_type      VARCHAR(5)      NULL                        COMMENT '血型：A/B/AB/O/未知',
    address         VARCHAR(200)    NULL                        COMMENT '家庭住址',
    avatar          VARCHAR(255)    NULL                        COMMENT '头像URL',
    status          TINYINT(1)      NOT NULL DEFAULT 1          COMMENT '状态：1正常 0禁用',
    last_login_at   DATETIME        NULL                        COMMENT '最后登录时间',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_id_card (id_card),
    UNIQUE KEY uk_phone (phone),
    KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='居民账号与基本信息';

-- ------------------------------------------------------------
-- 健康档案主表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS health_record (
    id                  BIGINT      NOT NULL AUTO_INCREMENT,
    resident_id         BIGINT      NOT NULL                    COMMENT '关联resident.id',
    allergy_history     TEXT        NULL                        COMMENT '过敏史（JSON数组）',
    past_medical_history TEXT       NULL                        COMMENT '既往病史（JSON数组）',
    family_history      TEXT        NULL                        COMMENT '家族病史',
    chronic_tags        VARCHAR(200) NULL                       COMMENT '慢病标签（逗号分隔）',
    emergency_contact   VARCHAR(50) NULL                        COMMENT '紧急联系人姓名',
    emergency_phone     VARCHAR(11) NULL                        COMMENT '紧急联系人电话',
    created_by          BIGINT      NULL                        COMMENT '建档医护人员staff.id',
    created_at          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted          TINYINT(1)  NOT NULL DEFAULT 0,
    deleted_at          DATETIME    NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_resident_id (resident_id),
    KEY idx_chronic_tags (chronic_tags)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='居民健康档案主表';

-- ------------------------------------------------------------
-- 预约挂号表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS appointment (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    appt_no         VARCHAR(30)     NOT NULL                    COMMENT '就诊号',
    resident_id     BIGINT          NOT NULL                    COMMENT '居民ID',
    slot_id         BIGINT          NOT NULL                    COMMENT '出诊时段ID',
    dept_code       VARCHAR(20)     NOT NULL                    COMMENT '科室编码（冗余）',
    dept_name       VARCHAR(50)     NOT NULL                    COMMENT '科室名称（冗余）',
    staff_id        BIGINT          NOT NULL                    COMMENT '预约医生ID',
    staff_name      VARCHAR(20)     NOT NULL                    COMMENT '医生姓名（冗余）',
    appt_date       DATE            NOT NULL                    COMMENT '预约日期',
    time_period     TINYINT(1)      NOT NULL                    COMMENT '时段：1上午 2下午',
    patient_name    VARCHAR(20)     NOT NULL                    COMMENT '就诊人姓名',
    patient_phone   VARCHAR(11)     NOT NULL                    COMMENT '就诊人手机号',
    symptom_desc    VARCHAR(200)    NULL                        COMMENT '症状描述',
    status          TINYINT(1)      NOT NULL DEFAULT 1          COMMENT '1待就诊 2接诊中 3已完成 4已取消 5未到诊',
    cancel_reason   VARCHAR(200)    NULL                        COMMENT '取消原因',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_appt_no (appt_no),
    KEY idx_resident_id (resident_id),
    KEY idx_slot_id (slot_id),
    KEY idx_appt_date (appt_date),
    KEY idx_staff_date (staff_id, appt_date),
    KEY idx_status (status),
    KEY idx_resident_dept_date (resident_id, dept_code, appt_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约挂号记录';

-- ------------------------------------------------------------
-- 就诊记录表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS visit_record (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    appointment_id  BIGINT          NOT NULL                    COMMENT '关联预约ID',
    resident_id     BIGINT          NOT NULL                    COMMENT '居民ID',
    staff_id        BIGINT          NOT NULL                    COMMENT '接诊医生ID',
    staff_name      VARCHAR(20)     NOT NULL                    COMMENT '医生姓名（冗余）',
    dept_code       VARCHAR(20)     NOT NULL                    COMMENT '科室编码（冗余）',
    dept_name       VARCHAR(50)     NOT NULL                    COMMENT '科室名称（冗余）',
    visit_date      DATE            NOT NULL                    COMMENT '就诊日期',
    chief_complaint VARCHAR(500)    NULL                        COMMENT '主诉',
    present_illness TEXT            NULL                        COMMENT '现病史',
    physical_exam   VARCHAR(500)    NULL                        COMMENT '体征',
    diagnosis_codes VARCHAR(500)    NULL                        COMMENT '诊断ICD编码',
    diagnosis_names VARCHAR(500)    NULL                        COMMENT '诊断名称',
    medical_advice  TEXT            NULL                        COMMENT '医嘱',
    revisit_date    DATE            NULL                        COMMENT '复诊提醒日期',
    revisit_note    VARCHAR(200)    NULL                        COMMENT '复诊提醒说明',
    finished_at     DATETIME        NULL                        COMMENT '接诊完结时间',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    KEY idx_resident_id (resident_id),
    KEY idx_appointment_id (appointment_id),
    KEY idx_visit_date (visit_date),
    KEY idx_staff_date (staff_id, visit_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊记录';

-- ------------------------------------------------------------
-- 处方主表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prescription (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    presc_no        VARCHAR(30)     NOT NULL                    COMMENT '处方编号',
    visit_id        BIGINT          NOT NULL                    COMMENT '关联就诊记录ID',
    resident_id     BIGINT          NOT NULL                    COMMENT '居民ID',
    staff_id        BIGINT          NOT NULL                    COMMENT '开方医生ID',
    staff_name      VARCHAR(20)     NOT NULL                    COMMENT '医生姓名（冗余）',
    status          TINYINT(1)      NOT NULL DEFAULT 1          COMMENT '1待发药 2已发药 3已撤销',
    pharmacist_id   BIGINT          NULL                        COMMENT '发药护士ID',
    pharmacist_name VARCHAR(20)     NULL                        COMMENT '发药护士姓名',
    dispensed_at    DATETIME        NULL                        COMMENT '发药时间',
    notes           VARCHAR(200)    NULL                        COMMENT '处方备注',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_presc_no (presc_no),
    KEY idx_visit_id (visit_id),
    KEY idx_resident_id (resident_id),
    KEY idx_status (status),
    KEY idx_staff_id (staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方主表';

-- ------------------------------------------------------------
-- 处方明细表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prescription_item (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    prescription_id BIGINT          NOT NULL                    COMMENT '关联处方ID',
    drug_id         BIGINT          NOT NULL                    COMMENT '药品ID',
    drug_name       VARCHAR(100)    NOT NULL                    COMMENT '药品通用名（冗余快照）',
    drug_spec       VARCHAR(100)    NOT NULL                    COMMENT '规格（冗余快照）',
    drug_unit       VARCHAR(20)     NOT NULL                    COMMENT '单位',
    quantity        INT             NOT NULL                    COMMENT '数量',
    usage_method    VARCHAR(20)     NOT NULL                    COMMENT '用法',
    dosage          VARCHAR(50)     NOT NULL                    COMMENT '用量',
    frequency       VARCHAR(30)     NOT NULL                    COMMENT '频次',
    days            TINYINT         NOT NULL                    COMMENT '天数',
    item_notes      VARCHAR(100)    NULL                        COMMENT '单项备注',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_prescription_id (prescription_id),
    KEY idx_drug_id (drug_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方明细（冗余药品快照）';

-- ------------------------------------------------------------
-- 疫苗接种预约表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vaccine_appointment (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    appt_no         VARCHAR(30)     NOT NULL                    COMMENT '接种预约号',
    resident_id     BIGINT          NOT NULL,
    vaccine_id      BIGINT          NOT NULL,
    vaccine_name    VARCHAR(100)    NOT NULL                    COMMENT '疫苗名称（冗余）',
    dose_num        TINYINT         NOT NULL DEFAULT 1          COMMENT '第几剂',
    appt_date       DATE            NOT NULL                    COMMENT '预约接种日期',
    time_period     TINYINT(1)      NOT NULL                    COMMENT '1上午 2下午',
    patient_name    VARCHAR(20)     NOT NULL,
    patient_phone   VARCHAR(11)     NOT NULL,
    status          TINYINT(1)      NOT NULL DEFAULT 1          COMMENT '1待接种 2已接种 3已取消',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,
    deleted_at      DATETIME        NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_appt_no (appt_no),
    KEY idx_resident_id (resident_id),
    KEY idx_vaccine_date (vaccine_id, appt_date),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗接种预约';

-- ------------------------------------------------------------
-- 接种记录表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vaccine_record (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    resident_id     BIGINT          NOT NULL,
    appt_id         BIGINT          NULL                        COMMENT '关联预约ID',
    vaccine_id      BIGINT          NOT NULL,
    vaccine_name    VARCHAR(100)    NOT NULL                    COMMENT '冗余快照',
    batch_no        VARCHAR(50)     NOT NULL                    COMMENT '疫苗批次号',
    dose_num        TINYINT         NOT NULL,
    injection_site  VARCHAR(30)     NOT NULL                    COMMENT '接种部位',
    dosage          VARCHAR(20)     NOT NULL                    COMMENT '接种剂量',
    staff_id        BIGINT          NOT NULL                    COMMENT '接种医护ID',
    staff_name      VARCHAR(20)     NOT NULL,
    adverse_reaction VARCHAR(200)   NULL                        COMMENT '不良反应',
    next_dose_date  DATE            NULL                        COMMENT '下次接种提醒日期',
    vaccinated_at   DATETIME        NOT NULL                    COMMENT '实际接种时间',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_resident_id (resident_id),
    KEY idx_vaccine_id (vaccine_id),
    KEY idx_vaccinated_at (vaccinated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='疫苗接种记录';

-- ------------------------------------------------------------
-- 站内消息表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS message (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    resident_id     BIGINT          NOT NULL                    COMMENT '消息接收居民',
    msg_type        VARCHAR(30)     NOT NULL                    COMMENT '类型',
    title           VARCHAR(100)    NOT NULL,
    content         TEXT            NOT NULL,
    related_id      BIGINT          NULL                        COMMENT '关联业务ID',
    is_read         TINYINT(1)      NOT NULL DEFAULT 0,
    read_at         DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_resident_id (resident_id),
    KEY idx_is_read (resident_id, is_read),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内消息';
