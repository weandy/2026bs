-- Phase 3: 创新点① 慢病健康趋势追踪 — 数据库迁移
-- 为 health_vital 表新增录入人信息字段

ALTER TABLE health_vital ADD COLUMN recorded_by BIGINT NULL COMMENT '录入医护人员ID';
ALTER TABLE health_vital ADD COLUMN recorded_by_name VARCHAR(50) NULL COMMENT '录入医护人员姓名';
