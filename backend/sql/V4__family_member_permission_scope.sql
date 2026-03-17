-- Phase 5: 创新点③ 家庭代管分级权限 — 数据库迁移
-- 为 family_member 表新增权限范围字段

ALTER TABLE family_member ADD COLUMN permission_scope VARCHAR(200) DEFAULT 'basic,appointment' COMMENT '代管权限范围(CSV)';
