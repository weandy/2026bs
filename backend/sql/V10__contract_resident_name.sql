-- V10: 家庭医生签约表补充居民姓名冗余字段
-- Bug6修复：添加 resident_name 字段，支持管理员按居民姓名搜索签约
ALTER TABLE family_doctor_contract
    ADD COLUMN resident_name VARCHAR(50) NULL COMMENT '居民姓名（冗余，用于关键词搜索）';
