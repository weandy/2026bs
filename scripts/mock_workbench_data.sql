-- 接诊工作台体验专用: 候诊号源模拟测试数据
-- 请在被操作的数据库环境 (chp_resident) 中直接执行以下脚本
USE chp_resident;

-- 变量设定: 请将 @staffId 替换为你当前登录的医生账号 ID
-- 我们默认先使用 2 (开发环境下医生账号大概率为 2)
SET @staffId = 2;

-- 必须非空的其他约束
SET @slotId = 1;
SET @deptCode = 'GP';
SET @deptName = '全科门诊';

-- 清理当日系统内该医生名下的旧测试数据，防止无限堆积
DELETE FROM appointment 
WHERE staff_id = @staffId 
  AND appt_date = CURDATE() 
  AND appt_no LIKE 'MOCK%';

-- 批量插入用于交互体验的挂号数据
INSERT INTO appointment 
(appt_no, resident_id, slot_id, dept_code, dept_name, staff_id, staff_name, appt_date, time_period, patient_name, patient_phone, status, created_at, updated_at, is_deleted)
VALUES
(CONCAT('MOCK', UNIX_TIMESTAMP(), '1'), 1001, @slotId, @deptCode, @deptName, @staffId, '系统模拟名', CURDATE(), 1, '李建国', '13810000001', 1, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW(), 0),
(CONCAT('MOCK', UNIX_TIMESTAMP(), '2'), 1002, @slotId, @deptCode, @deptName, @staffId, '系统模拟名', CURDATE(), 1, '王秀兰', '13810000002', 1, DATE_SUB(NOW(), INTERVAL 25 MINUTE), NOW(), 0),
(CONCAT('MOCK', UNIX_TIMESTAMP(), '3'), 1003, @slotId, @deptCode, @deptName, @staffId, '系统模拟名', CURDATE(), 1, '张伟',   '13810000003', 1, DATE_SUB(NOW(), INTERVAL 20 MINUTE), NOW(), 0),
(CONCAT('MOCK', UNIX_TIMESTAMP(), '4'), 1004, @slotId, @deptCode, @deptName, @staffId, '系统模拟名', CURDATE(), 2, '赵建平', '13810000004', 1, DATE_SUB(NOW(), INTERVAL 15 MINUTE), NOW(), 0),
(CONCAT('MOCK', UNIX_TIMESTAMP(), '5'), 1005, @slotId, @deptCode, @deptName, @staffId, '系统模拟名', CURDATE(), 2, '孙丽华', '13810000005', 1, DATE_SUB(NOW(), INTERVAL 10 MINUTE), NOW(), 0),
(CONCAT('MOCK', UNIX_TIMESTAMP(), '6'), 1006, @slotId, @deptCode, @deptName, @staffId, '系统模拟名', CURDATE(), 2, '陈强',   '13810000006', 1, DATE_SUB(NOW(), INTERVAL  5 MINUTE), NOW(), 0);
