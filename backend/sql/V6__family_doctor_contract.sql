-- V6: 家庭医生签约表 (按业务流设计文档建表)
-- 如果已有旧结构的表，先备份再重建
DROP TABLE IF EXISTS family_doctor_contract;

CREATE TABLE family_doctor_contract (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    resident_id    BIGINT       NOT NULL COMMENT '居民用户ID',
    doctor_id      BIGINT       NOT NULL COMMENT '签约医生用户ID',
    doctor_name    VARCHAR(50)  NULL     COMMENT '医生姓名（冗余）',
    team_name      VARCHAR(100) NULL     COMMENT '家庭医生团队名称',
    nurse_id       BIGINT       NULL     COMMENT '团队护士ID',
    nurse_name     VARCHAR(50)  NULL     COMMENT '护士姓名',
    status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/ACTIVE/REJECTED/EXPIRED/CANCELLED',
    apply_reason   VARCHAR(500) NULL     COMMENT '签约原因（居民填写）',
    reject_reason  VARCHAR(500) NULL     COMMENT '驳回原因（管理员填写）',
    cancel_reason  VARCHAR(500) NULL     COMMENT '解约原因',
    operator_id    BIGINT       NULL     COMMENT '审核操作的管理员ID',
    apply_time     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    approve_time   DATETIME     NULL     COMMENT '审核时间',
    start_date     DATE         NULL     COMMENT '签约生效日期',
    end_date       DATE         NULL     COMMENT '签约到期日期',
    service_package VARCHAR(200) NULL    COMMENT '服务包',
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_resident_status (resident_id, status),
    INDEX idx_doctor_status (doctor_id, status)
) COMMENT '家庭医生签约';
