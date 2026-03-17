-- V7: 医护人员个人简介扩展表
-- 因 sys_user 表可能不宜直接加字段，改用独立表关联
CREATE TABLE IF NOT EXISTS staff_profile (
    id           BIGINT PRIMARY KEY,          -- 与 sys_user.id 一致（1:1）
    specialty    VARCHAR(200) DEFAULT '',     -- 擅长领域
    bio          TEXT         DEFAULT '',     -- 个人简介
    title        VARCHAR(50)  DEFAULT '',     -- 职称（主任医师/副主任医师/主治医师/住院医师等）
    years_exp    INT          DEFAULT 0,      -- 从医年限
    avatar_url   VARCHAR(255) DEFAULT '',     -- 头像URL（预留）
    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
