-- V9: 家庭成员表扩展字段 (设计文档 P1 改造)
-- 添加出生日期、性别、备注字段
-- Bug14修复: 去掉 IF NOT EXISTS (MySQL 5.7 不支持)，Flyway 保证本脚本只执行一次
ALTER TABLE family_member
    ADD COLUMN birth_date DATE         NULL COMMENT '出生日期',
    ADD COLUMN gender     TINYINT      NULL COMMENT '1男 2女',
    ADD COLUMN remark     VARCHAR(200) NULL COMMENT '备注（如：高血压长期随访患者）';
