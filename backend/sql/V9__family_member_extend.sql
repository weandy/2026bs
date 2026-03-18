-- V9: 家庭成员表扩展字段 (设计文档 P1 改造)
-- 添加出生日期、性别、备注字段
ALTER TABLE family_member
    ADD COLUMN IF NOT EXISTS birth_date DATE         NULL COMMENT '出生日期',
    ADD COLUMN IF NOT EXISTS gender     TINYINT      NULL COMMENT '1男 2女',
    ADD COLUMN IF NOT EXISTS remark     VARCHAR(200) NULL COMMENT '备注（如：高血压长期随访患者）';
