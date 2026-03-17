-- V5: 疫苗相关表创建
-- vaccine_stock (被 admin VaccineStock 和 resident VaccineStockVO 共用)
-- vaccine_appointment (居民端疫苗预约)
-- vaccine_record (接种记录)

CREATE TABLE IF NOT EXISTS vaccine_stock (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    vaccine_name VARCHAR(100)  NOT NULL COMMENT '疫苗名称',
    vaccine_code VARCHAR(50)   NULL     COMMENT '疫苗编码',
    manufacturer VARCHAR(100)  NULL     COMMENT '生产厂商',
    batch_no     VARCHAR(50)   NULL     COMMENT '批号',
    expiry_date  DATE          NULL     COMMENT '有效期至',
    quantity     INT           NOT NULL DEFAULT 0 COMMENT '库存数量',
    alert_qty    INT           NOT NULL DEFAULT 10 COMMENT '预警阈值',
    status       TINYINT       NOT NULL DEFAULT 1 COMMENT '1-正常 0-停用',
    created_at   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '疫苗库存';

CREATE TABLE IF NOT EXISTS vaccine_appointment (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    appt_no       VARCHAR(30)  NULL     COMMENT '预约编号',
    resident_id   BIGINT       NOT NULL COMMENT '居民ID',
    vaccine_id    BIGINT       NULL     COMMENT '疫苗库存ID',
    vaccine_name  VARCHAR(100) NULL     COMMENT '疫苗名称',
    dose_num      INT          DEFAULT 1 COMMENT '第几剂',
    appt_date     DATE         NOT NULL COMMENT '预约日期',
    time_period   TINYINT      DEFAULT 1 COMMENT '1-上午 2-下午 3-晚上',
    patient_name  VARCHAR(50)  NULL     COMMENT '接种人姓名',
    patient_phone VARCHAR(20)  NULL     COMMENT '联系电话',
    status        TINYINT      DEFAULT 1 COMMENT '1-待接种 2-已接种 3-已取消',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted    TINYINT      DEFAULT 0,
    deleted_at    DATETIME     NULL
) COMMENT '疫苗预约';

CREATE TABLE IF NOT EXISTS vaccine_record (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    resident_id      BIGINT       NOT NULL COMMENT '居民ID',
    appt_id          BIGINT       NULL     COMMENT '关联预约ID',
    vaccine_id       BIGINT       NULL     COMMENT '疫苗库存ID',
    vaccine_name     VARCHAR(100) NULL     COMMENT '疫苗名称',
    batch_no         VARCHAR(50)  NULL     COMMENT '批号',
    dose_num         INT          DEFAULT 1 COMMENT '第几剂',
    injection_site   VARCHAR(50)  NULL     COMMENT '接种部位',
    dosage           VARCHAR(50)  NULL     COMMENT '剂量',
    staff_id         BIGINT       NULL     COMMENT '接种医护ID',
    staff_name       VARCHAR(50)  NULL     COMMENT '接种医护姓名',
    adverse_reaction VARCHAR(500) NULL     COMMENT '不良反应',
    next_dose_date   DATE         NULL     COMMENT '下针日期',
    vaccinated_at    DATETIME     NULL     COMMENT '接种时间',
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP
) COMMENT '接种记录';

-- 示例疫苗库存数据
INSERT INTO vaccine_stock (vaccine_name, vaccine_code, manufacturer, quantity, alert_qty, expiry_date) VALUES
('流感疫苗（四价）',    'IIV4',     '华兰生物',   200, 20, '2027-06-30'),
('新冠疫苗（重组）',    'COVID-REC','智飞生物',    150, 15, '2027-03-31'),
('乙肝疫苗',            'HepB',     '康泰生物',   300, 30, '2027-12-31'),
('水痘疫苗',            'VZV',      '长春百克',    80,  10, '2027-09-30'),
('HPV九价疫苗',         'HPV9',     '默沙东',      50,  5,  '2027-08-31'),
('肺炎疫苗（23价）',    'PPV23',    '成大生物',   120, 12, '2027-10-31');
