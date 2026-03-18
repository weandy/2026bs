-- V11: 全系统仿真数据补充
-- 覆盖：health_vital / follow_up_record / vaccine_record
--        prescription + prescription_item / message / visit_record(密度补充)
-- 全部使用 INSERT IGNORE，幂等安全，不破坏 V8 现有数据
-- ============================================================

-- ============================================================
-- 1. health_vital（生命体征历史数据）
--    居民 R001=1001 高血压+糖尿病，R002=1002 糖尿病，R003=1003 高血压
--    vitalType: blood_pressure / blood_glucose / weight
-- ============================================================
INSERT IGNORE INTO health_vital
  (resident_id, vital_type, vital_value, measure_time, note, recorded_by, recorded_by_name, created_at)
VALUES
-- R001 血压（过去 6 个月，每月 2 次）
(1001,'blood_pressure','142/90',NOW()-INTERVAL 180 DAY,'随访记录',2001,'李医生',NOW()-INTERVAL 180 DAY),
(1001,'blood_pressure','138/88',NOW()-INTERVAL 150 DAY,'门诊测量',2001,'李医生',NOW()-INTERVAL 150 DAY),
(1001,'blood_pressure','145/92',NOW()-INTERVAL 120 DAY,'随访记录',2001,'李医生',NOW()-INTERVAL 120 DAY),
(1001,'blood_pressure','140/88',NOW()-INTERVAL 90 DAY, '随访记录',2001,'李医生',NOW()-INTERVAL 90 DAY),
(1001,'blood_pressure','136/86',NOW()-INTERVAL 60 DAY, '自测上报',NULL,NULL,NOW()-INTERVAL 60 DAY),
(1001,'blood_pressure','132/84',NOW()-INTERVAL 45 DAY, '门诊测量',2001,'李医生',NOW()-INTERVAL 45 DAY),
(1001,'blood_pressure','135/87',NOW()-INTERVAL 30 DAY, '随访记录',2001,'李医生',NOW()-INTERVAL 30 DAY),
(1001,'blood_pressure','130/85',NOW()-INTERVAL 15 DAY, '自测上报',NULL,NULL,NOW()-INTERVAL 15 DAY),
(1001,'blood_pressure','133/86',NOW()-INTERVAL 7 DAY,  '门诊测量',2001,'李医生',NOW()-INTERVAL 7 DAY),
(1001,'blood_pressure','128/82',NOW()-INTERVAL 2 DAY,  '自测上报',NULL,NULL,NOW()-INTERVAL 2 DAY),

-- R001 血糖（空腹）
(1001,'blood_glucose','7.2',NOW()-INTERVAL 175 DAY,'随访记录',2001,'李医生',NOW()-INTERVAL 175 DAY),
(1001,'blood_glucose','6.8',NOW()-INTERVAL 145 DAY,'门诊测量',2001,'李医生',NOW()-INTERVAL 145 DAY),
(1001,'blood_glucose','7.5',NOW()-INTERVAL 112 DAY,'随访记录',2001,'李医生',NOW()-INTERVAL 112 DAY),
(1001,'blood_glucose','6.5',NOW()-INTERVAL 85 DAY, '门诊测量',2001,'李医生',NOW()-INTERVAL 85 DAY),
(1001,'blood_glucose','6.9',NOW()-INTERVAL 55 DAY, '随访记录',2001,'李医生',NOW()-INTERVAL 55 DAY),
(1001,'blood_glucose','6.2',NOW()-INTERVAL 28 DAY, '门诊测量',2001,'李医生',NOW()-INTERVAL 28 DAY),
(1001,'blood_glucose','6.4',NOW()-INTERVAL 10 DAY, '自测上报',NULL,NULL,NOW()-INTERVAL 10 DAY),

-- R001 体重
(1001,'weight','71.5',NOW()-INTERVAL 180 DAY,'定期体检',2001,'李医生',NOW()-INTERVAL 180 DAY),
(1001,'weight','72.0',NOW()-INTERVAL 120 DAY,'定期体检',2001,'李医生',NOW()-INTERVAL 120 DAY),
(1001,'weight','71.0',NOW()-INTERVAL 60 DAY, '定期体检',2001,'李医生',NOW()-INTERVAL 60 DAY),
(1001,'weight','70.5',NOW()-INTERVAL 30 DAY, '自测上报',NULL,NULL,NOW()-INTERVAL 30 DAY),
(1001,'weight','70.0',NOW()-INTERVAL 7 DAY,  '自测上报',NULL,NULL,NOW()-INTERVAL 7 DAY),

-- R002 血糖（李敏，2型糖尿病）
(1002,'blood_glucose','8.5',NOW()-INTERVAL 160 DAY,'随访记录',2002,'赵医生',NOW()-INTERVAL 160 DAY),
(1002,'blood_glucose','7.8',NOW()-INTERVAL 130 DAY,'门诊测量',2002,'赵医生',NOW()-INTERVAL 130 DAY),
(1002,'blood_glucose','8.2',NOW()-INTERVAL 100 DAY,'随访记录',2002,'赵医生',NOW()-INTERVAL 100 DAY),
(1002,'blood_glucose','7.5',NOW()-INTERVAL 70 DAY, '门诊测量',2002,'赵医生',NOW()-INTERVAL 70 DAY),
(1002,'blood_glucose','7.1',NOW()-INTERVAL 40 DAY, '随访记录',2002,'赵医生',NOW()-INTERVAL 40 DAY),
(1002,'blood_glucose','6.9',NOW()-INTERVAL 15 DAY, '门诊测量',2002,'赵医生',NOW()-INTERVAL 15 DAY),
(1002,'blood_glucose','7.0',NOW()-INTERVAL 5 DAY,  '自测上报',NULL,NULL,NOW()-INTERVAL 5 DAY),

-- R002 血压
(1002,'blood_pressure','118/75',NOW()-INTERVAL 155 DAY,'随访记录',2002,'赵医生',NOW()-INTERVAL 155 DAY),
(1002,'blood_pressure','120/78',NOW()-INTERVAL 90 DAY, '门诊测量',2002,'赵医生',NOW()-INTERVAL 90 DAY),
(1002,'blood_pressure','116/74',NOW()-INTERVAL 30 DAY, '门诊测量',2002,'赵医生',NOW()-INTERVAL 30 DAY),

-- R003 血压（王芳，高血压）
(1003,'blood_pressure','155/95',NOW()-INTERVAL 140 DAY,'随访记录',2001,'李医生',NOW()-INTERVAL 140 DAY),
(1003,'blood_pressure','150/92',NOW()-INTERVAL 110 DAY,'门诊测量',2001,'李医生',NOW()-INTERVAL 110 DAY),
(1003,'blood_pressure','148/90',NOW()-INTERVAL 80 DAY, '随访记录',2001,'李医生',NOW()-INTERVAL 80 DAY),
(1003,'blood_pressure','145/88',NOW()-INTERVAL 50 DAY, '门诊测量',2001,'李医生',NOW()-INTERVAL 50 DAY),
(1003,'blood_pressure','142/86',NOW()-INTERVAL 20 DAY, '随访记录',2001,'李医生',NOW()-INTERVAL 20 DAY),
(1003,'blood_pressure','140/85',NOW()-INTERVAL 5 DAY,  '自测上报',NULL,NULL,NOW()-INTERVAL 5 DAY);

-- ============================================================
-- 2. follow_up_record（随访记录，关联已有计划 9001/9002）
-- ============================================================
INSERT IGNORE INTO follow_up_record
  (plan_id, resident_id, follow_date, follow_method, systolic_bp, diastolic_bp,
   fasting_glucose, postprandial_glucose, medication_compliance, lifestyle_issues,
   health_guidance, next_follow_date, staff_id, staff_name, created_at)
VALUES
-- plan 9001：R001 高血压随访（已完成）
(9001, 1001, CURDATE()-INTERVAL 5 DAY, 1, 138, 88, 6.5, NULL, 1,
 '偶尔忘记服药，喜食重口味',
 '建议规律服药，低盐低脂饮食，每天步行30分钟',
 CURDATE()+INTERVAL 25 DAY, 2001, '李医生', NOW()-INTERVAL 5 DAY),

-- plan 9002：R002 糖尿病随访（已完成）
(9002, 1002, CURDATE()-INTERVAL 3 DAY, 1, 120, 76, 7.1, 9.8, 2,
 '饮食控制不规律，运动较少',
 '坚持每天30分钟有氧运动，控制主食摄入，监测餐后血糖',
 CURDATE()+INTERVAL 20 DAY, 2002, '赵医生', NOW()-INTERVAL 3 DAY),

-- R001 3个月前的历史随访记录（基于前一个周期计划，手动补充）
(9001, 1001, CURDATE()-INTERVAL 95 DAY, 2, 142, 90, 6.8, NULL, 1,
 '生活压力大，情绪紧张',
 '放松心情，坚持用药，建议心理疏导',
 CURDATE()-INTERVAL 65 DAY, 2001, '李医生', NOW()-INTERVAL 95 DAY);

-- ============================================================
-- 3. vaccine_record（接种完成记录，关联已完成的预约）
-- ============================================================
INSERT IGNORE INTO vaccine_record
  (resident_id, appt_id, vaccine_id, vaccine_name, batch_no, dose_num,
   injection_site, dosage, staff_id, staff_name, adverse_reaction,
   next_dose_date, vaccinated_at, created_at)
VALUES
-- R001 新冠疫苗第1剂（appt 10004）
(1001, 10004, 1, '新冠灭活疫苗', 'BN20260101A', 1,
 '左上臂三角肌', '0.5mL', 3002, '陈护士', '注射部位轻微酸痛，次日消退',
 CURDATE()-INTERVAL 30 DAY+INTERVAL 28 DAY,
 NOW()-INTERVAL 30 DAY, NOW()-INTERVAL 30 DAY),

-- R001 新冠疫苗第2剂（28天后）
(1001, NULL, 1, '新冠灭活疫苗', 'BN20260101B', 2,
 '左上臂三角肌', '0.5mL', 3002, '陈护士', '无不适',
 NULL,
 NOW()-INTERVAL 2 DAY, NOW()-INTERVAL 2 DAY),

-- R002 流感疫苗（appt 10005）
(1002, 10005, 2, '四价流感病毒裂解疫苗', 'IN20260201C', 1,
 '右上臂三角肌', '0.5mL', 3002, '陈护士', '无',
 NULL,
 NOW()-INTERVAL 20 DAY, NOW()-INTERVAL 20 DAY),

-- R003 乙肝疫苗历史记录
(1003, NULL, 3, '重组乙型肝炎疫苗', 'HB20251101A', 1,
 '右上臂三角肌', '0.5mL', 3002, '陈护士', '无',
 NOW()-INTERVAL 140 DAY+INTERVAL 28 DAY,
 NOW()-INTERVAL 140 DAY, NOW()-INTERVAL 140 DAY),

(1003, NULL, 3, '重组乙型肝炎疫苗', 'HB20251201B', 2,
 '右上臂三角肌', '0.5mL', 3002, '陈护士', '无',
 NOW()-INTERVAL 140 DAY+INTERVAL 56 DAY,
 NOW()-INTERVAL 112 DAY, NOW()-INTERVAL 112 DAY),

(1003, NULL, 3, '重组乙型肝炎疫苗', 'HB20260101C', 3,
 '右上臂三角肌', '0.5mL', 3002, '陈护士', '无',
 NULL,
 NOW()-INTERVAL 30 DAY, NOW()-INTERVAL 30 DAY);

-- ============================================================
-- 4. prescription + prescription_item（处方，关联就诊记录）
-- ============================================================
INSERT IGNORE INTO prescription
  (id, presc_no, visit_id, resident_id, staff_id, staff_name, status, notes, created_at, updated_at)
VALUES
(11001, 'RX20260308001', 7001, 1001, 2001, '李医生', 2, '急性上呼吸道感染，抗感染+退热治疗', NOW()-INTERVAL 10 DAY, NOW()-INTERVAL 10 DAY),
(11002, 'RX20260310001', 7002, 1002, 2002, '赵医生', 2, '血糖控制强化，调整降糖方案',        NOW()-INTERVAL 8 DAY, NOW()-INTERVAL 8 DAY),
(11003, 'RX20260313001', 7003, 1003, 2001, '李医生', 2, '急性支气管炎，止咳化痰',            NOW()-INTERVAL 5 DAY, NOW()-INTERVAL 5 DAY),
(11004, 'RX20260315001', 7005, 1001, 2001, '李医生', 2, '偏头痛发作，止痛+预防治疗',         NOW()-INTERVAL 1 DAY, NOW()-INTERVAL 1 DAY);

INSERT IGNORE INTO prescription_item
  (prescription_id, drug_id, drug_name, drug_spec, drug_unit, quantity,
   usage_method, dosage, frequency, days, created_at)
VALUES
-- 处方 11001（急性上感）
(11001, NULL, '布洛芬缓释胶囊', '0.3g/粒', '粒', 12, '口服', '0.3g', '每日2次', 3, NOW()-INTERVAL 10 DAY),
(11001, NULL, '头孢克肟分散片', '0.1g/片', '片', 14, '口服', '0.1g', '每日2次', 7, NOW()-INTERVAL 10 DAY),
(11001, NULL, '盐酸氨溴索口服液', '100mL:0.3g', 'mL', 1, '口服', '10mL', '每日3次', 5, NOW()-INTERVAL 10 DAY),

-- 处方 11002（糖尿病）
(11002, NULL, '盐酸二甲双胍缓释片', '0.5g/片', '片', 60, '口服(餐中)', '0.5g', '每日2次', 30, NOW()-INTERVAL 8 DAY),
(11002, NULL, '格列齐特缓释片', '30mg/片', '片', 30, '口服(早餐前)', '30mg', '每日1次', 30, NOW()-INTERVAL 8 DAY),

-- 处方 11003（急性支气管炎）
(11003, NULL, '阿奇霉素肠溶胶囊', '0.25g/粒', '粒', 6, '口服', '0.5g', '每日1次', 3, NOW()-INTERVAL 5 DAY),
(11003, NULL, '氨溴特罗口服液', '100mL', 'mL', 1, '口服', '10mL', '每日2次', 7, NOW()-INTERVAL 5 DAY),

-- 处方 11004（偏头痛）
(11004, NULL, '布洛芬片', '0.2g/片', '片', 10, '口服', '0.4g', '必要时(头痛发作)', 5, NOW()-INTERVAL 1 DAY),
(11004, NULL, '氟桂利嗪胶囊', '5mg/粒', '粒', 20, '口服(睡前)', '10mg', '每日1次', 10, NOW()-INTERVAL 1 DAY);

-- ============================================================
-- 5. message（系统消息通知）
-- ============================================================
INSERT IGNORE INTO message
  (resident_id, msg_type, title, content, related_id, is_read, created_at)
VALUES
-- R001 消息
(1001, 'APPOINTMENT', '预约提醒', '您于明日上午在全科门诊有一次复诊预约（李医生），请准时到诊。', 6005, 0, NOW()-INTERVAL 2 DAY),
(1001, 'FOLLOW_UP',   '随访提醒', '您的高血压随访计划已完成，下次随访日期为25天后，请关注血压变化。', 9001, 0, NOW()-INTERVAL 5 DAY),
(1001, 'VACCINE',     '接种完成通知', '您于近日完成新冠灭活疫苗第2剂接种，请注意留观30分钟。接种点：社区疫苗接种科。', NULL, 1, NOW()-INTERVAL 2 DAY),
(1001, 'SYSTEM',      '健康提醒', '根据您近期血压监测数据，收缩压偏高，建议坚持低盐饮食并按时服药。', NULL, 1, NOW()-INTERVAL 10 DAY),
(1001, 'CONTRACT',    '签约通知', '您的家庭医生签约申请已审核通过，签约医生：李医生（全科门诊），有效期1年。', 8001, 1, NOW()-INTERVAL 30 DAY),

-- R002 消息
(1002, 'FOLLOW_UP',  '随访提醒', '您的糖尿病随访计划已完成，下次随访约20天后，请注意控制饮食和监测血糖。', 9002, 0, NOW()-INTERVAL 3 DAY),
(1002, 'SYSTEM',     '健康提醒', '您近期空腹血糖偏高（7.1 mmol/L），建议增加运动频率，如波动较大请及时就诊。', NULL, 0, NOW()-INTERVAL 5 DAY),
(1002, 'VACCINE',    '接种完成通知', '您已完成四价流感疫苗接种，免疫效果通常于接种后2周建立，期间注意防护。', NULL, 1, NOW()-INTERVAL 20 DAY),

-- R003 消息
(1003, 'APPOINTMENT', '预约提醒', '您在3天前的门诊就诊记录已出具，主诊：李医生。', 7003, 1, NOW()-INTERVAL 4 DAY),
(1003, 'SYSTEM',      '健康提醒', '您的乙型肝炎疫苗全程接种已完成（3剂），建议1～2个月后检测乙肝表面抗体。', NULL, 0, NOW()-INTERVAL 25 DAY);

-- ============================================================
-- 6. visit_record（近14天就诊记录补充，丰富 Dashboard 趋势数据）
-- ============================================================
INSERT IGNORE INTO visit_record
  (id, resident_id, staff_id, staff_name, dept_name, visit_date,
   chief_complaint, diagnosis, diagnosis_names, status, created_at)
VALUES
-- 今天 -14 天 ~ 今天，每天 2~3 条，覆盖不同科室
(17001,1006,2001,'李医生','全科门诊',  NOW()-INTERVAL 14 DAY,'发热乏力','J06.9','急性上呼吸道感染',2,NOW()-INTERVAL 14 DAY),
(17002,1007,2002,'赵医生','慢病管理科',NOW()-INTERVAL 14 DAY,'血糖升高','E11.9','2型糖尿病随访',  2,NOW()-INTERVAL 14 DAY),

(17003,1008,2001,'李医生','全科门诊',  NOW()-INTERVAL 13 DAY,'头晕头痛','I10','高血压',2,NOW()-INTERVAL 13 DAY),
(17004,1009,2003,'王医生','儿科',      NOW()-INTERVAL 13 DAY,'儿童发热','R50.9','发热待查',2,NOW()-INTERVAL 13 DAY),

(17005,1010,2002,'赵医生','慢病管理科',NOW()-INTERVAL 12 DAY,'血压不稳','I10','高血压2级',2,NOW()-INTERVAL 12 DAY),
(17006,1005,2001,'李医生','全科门诊',  NOW()-INTERVAL 12 DAY,'咽痛咳嗽','J02.9','急性咽炎',2,NOW()-INTERVAL 12 DAY),

(17007,1004,2001,'李医生','全科门诊',  NOW()-INTERVAL 11 DAY,'胸闷气促','I20.9','心绞痛',2,NOW()-INTERVAL 11 DAY),
(17008,1003,2002,'赵医生','慢病管理科',NOW()-INTERVAL 11 DAY,'慢病随访','E11.9','2型糖尿病',2,NOW()-INTERVAL 11 DAY),
(17009,1009,2003,'王医生','儿科',      NOW()-INTERVAL 11 DAY,'儿童腹痛','R10.4','腹痛待查',2,NOW()-INTERVAL 11 DAY),

(17010,1008,2001,'李医生','全科门诊',  NOW()-INTERVAL 10 DAY,'腰背疼痛','M54.5','腰痛',2,NOW()-INTERVAL 10 DAY),
(17011,1006,2002,'赵医生','慢病管理科',NOW()-INTERVAL 10 DAY,'血糖监测','E11.65','2型糖尿病伴足部并发症',2,NOW()-INTERVAL 10 DAY),

(17012,1010,2001,'李医生','全科门诊',  NOW()-INTERVAL 9 DAY,'皮肤瘙痒','L29.9','瘙痒症',2,NOW()-INTERVAL 9 DAY),
(17013,1007,2003,'王医生','儿科',      NOW()-INTERVAL 9 DAY,'儿童体检','Z00.1','儿童健康检查',2,NOW()-INTERVAL 9 DAY),

(17014,1005,2001,'李医生','全科门诊',  NOW()-INTERVAL 8 DAY,'乏力失眠','F51.0','非器质性失眠症',2,NOW()-INTERVAL 8 DAY),
(17015,1004,2002,'赵医生','慢病管理科',NOW()-INTERVAL 8 DAY,'血压控制','I10','高血压随访',2,NOW()-INTERVAL 8 DAY),
(17016,1003,2001,'李医生','全科门诊',  NOW()-INTERVAL 8 DAY,'感冒咳嗽','J06.9','急性上呼吸道感染',2,NOW()-INTERVAL 8 DAY),

(17017,1002,2002,'赵医生','慢病管理科',NOW()-INTERVAL 7 DAY,'血糖偏高','E11.9','2型糖尿病',2,NOW()-INTERVAL 7 DAY),
(17018,1009,2001,'李医生','全科门诊',  NOW()-INTERVAL 7 DAY,'肩周疼痛','M75.1','肩袖综合征',2,NOW()-INTERVAL 7 DAY),

(17019,1006,2001,'李医生','全科门诊',  NOW()-INTERVAL 6 DAY,'反复头痛','G43.9','偏头痛',2,NOW()-INTERVAL 6 DAY),
(17020,1010,2003,'王医生','儿科',      NOW()-INTERVAL 6 DAY,'小儿咳嗽','J20.9','急性支气管炎',2,NOW()-INTERVAL 6 DAY),

(17021,1008,2002,'赵医生','慢病管理科',NOW()-INTERVAL 5 DAY,'慢阻肺随访','J44.1','慢阻肺急性加重',2,NOW()-INTERVAL 5 DAY),
(17022,1005,2001,'李医生','全科门诊',  NOW()-INTERVAL 5 DAY,'腹泻腹痛','K59.1','功能性腹泻',2,NOW()-INTERVAL 5 DAY),

(17023,1007,2001,'李医生','全科门诊',  NOW()-INTERVAL 4 DAY,'眩晕',    'R42','眩晕综合征',2,NOW()-INTERVAL 4 DAY),
(17024,1004,2002,'赵医生','慢病管理科',NOW()-INTERVAL 4 DAY,'血压偏高', 'I10','高血压',2,NOW()-INTERVAL 4 DAY),
(17025,1009,2003,'王医生','儿科',      NOW()-INTERVAL 4 DAY,'儿童呕吐', 'R11','恶心呕吐',2,NOW()-INTERVAL 4 DAY),

(17026,1003,2001,'李医生','全科门诊',  NOW()-INTERVAL 3 DAY,'膝关节痛','M17.9','膝骨关节炎',2,NOW()-INTERVAL 3 DAY),
(17027,1010,2002,'赵医生','慢病管理科',NOW()-INTERVAL 3 DAY,'血糖管理', 'E11.9','2型糖尿病',2,NOW()-INTERVAL 3 DAY),

(17028,1006,2001,'李医生','全科门诊',  NOW()-INTERVAL 2 DAY,'尿频尿痛','N30.0','急性膀胱炎',2,NOW()-INTERVAL 2 DAY),
(17029,1008,2003,'王医生','儿科',      NOW()-INTERVAL 2 DAY,'小儿湿疹','L20','特应性皮炎',2,NOW()-INTERVAL 2 DAY),

(17030,1005,2001,'李医生','全科门诊',  NOW()-INTERVAL 1 DAY,'肠胃不适','K30','功能性消化不良',2,NOW()-INTERVAL 1 DAY),
(17031,1007,2002,'赵医生','慢病管理科',NOW()-INTERVAL 1 DAY,'高血压', 'I10','高血压2级',2,NOW()-INTERVAL 1 DAY);
-- V11-fix: 为正确居民 ID（2=张建国，3=李秀兰，4=王志强）插入 health_vital 数据
-- 使用 INSERT IGNORE 幂等安全

-- ============================================================
-- 张建国 (id=2)：高血压患者，6个月血压/血糖/体重数据
-- ============================================================
INSERT IGNORE INTO health_vital (resident_id, vital_type, vital_value, measure_time, note, created_at) VALUES
-- 血压 (过去 6 个月)
(2,'blood_pressure','142/90',DATE_SUB(NOW(), INTERVAL 180 DAY),'随访记录',DATE_SUB(NOW(), INTERVAL 180 DAY)),
(2,'blood_pressure','138/88',DATE_SUB(NOW(), INTERVAL 150 DAY),'门诊测量',DATE_SUB(NOW(), INTERVAL 150 DAY)),
(2,'blood_pressure','145/92',DATE_SUB(NOW(), INTERVAL 120 DAY),'随访记录',DATE_SUB(NOW(), INTERVAL 120 DAY)),
(2,'blood_pressure','140/88',DATE_SUB(NOW(), INTERVAL 90 DAY), '随访记录',DATE_SUB(NOW(), INTERVAL 90 DAY)),
(2,'blood_pressure','136/86',DATE_SUB(NOW(), INTERVAL 60 DAY), '自测上报',DATE_SUB(NOW(), INTERVAL 60 DAY)),
(2,'blood_pressure','132/84',DATE_SUB(NOW(), INTERVAL 45 DAY), '门诊测量',DATE_SUB(NOW(), INTERVAL 45 DAY)),
(2,'blood_pressure','135/87',DATE_SUB(NOW(), INTERVAL 30 DAY), '随访记录',DATE_SUB(NOW(), INTERVAL 30 DAY)),
(2,'blood_pressure','130/85',DATE_SUB(NOW(), INTERVAL 15 DAY), '自测上报',DATE_SUB(NOW(), INTERVAL 15 DAY)),
(2,'blood_pressure','133/86',DATE_SUB(NOW(), INTERVAL 7 DAY),  '门诊测量',DATE_SUB(NOW(), INTERVAL 7 DAY)),
(2,'blood_pressure','128/82',DATE_SUB(NOW(), INTERVAL 2 DAY),  '自测上报',DATE_SUB(NOW(), INTERVAL 2 DAY)),
-- 血糖
(2,'blood_glucose','7.2',DATE_SUB(NOW(), INTERVAL 175 DAY),'随访记录',DATE_SUB(NOW(), INTERVAL 175 DAY)),
(2,'blood_glucose','6.8',DATE_SUB(NOW(), INTERVAL 145 DAY),'门诊测量',DATE_SUB(NOW(), INTERVAL 145 DAY)),
(2,'blood_glucose','7.5',DATE_SUB(NOW(), INTERVAL 112 DAY),'随访记录',DATE_SUB(NOW(), INTERVAL 112 DAY)),
(2,'blood_glucose','6.5',DATE_SUB(NOW(), INTERVAL 85 DAY), '门诊测量',DATE_SUB(NOW(), INTERVAL 85 DAY)),
(2,'blood_glucose','6.9',DATE_SUB(NOW(), INTERVAL 55 DAY), '随访记录',DATE_SUB(NOW(), INTERVAL 55 DAY)),
(2,'blood_glucose','6.2',DATE_SUB(NOW(), INTERVAL 28 DAY), '门诊测量',DATE_SUB(NOW(), INTERVAL 28 DAY)),
(2,'blood_glucose','6.4',DATE_SUB(NOW(), INTERVAL 10 DAY), '自测上报',DATE_SUB(NOW(), INTERVAL 10 DAY)),
-- 体重
(2,'weight','71.5',DATE_SUB(NOW(), INTERVAL 180 DAY),'定期体检',DATE_SUB(NOW(), INTERVAL 180 DAY)),
(2,'weight','72.0',DATE_SUB(NOW(), INTERVAL 120 DAY),'定期体检',DATE_SUB(NOW(), INTERVAL 120 DAY)),
(2,'weight','71.0',DATE_SUB(NOW(), INTERVAL 60 DAY), '定期体检',DATE_SUB(NOW(), INTERVAL 60 DAY)),
(2,'weight','70.5',DATE_SUB(NOW(), INTERVAL 30 DAY), '自测上报',DATE_SUB(NOW(), INTERVAL 30 DAY)),
(2,'weight','70.0',DATE_SUB(NOW(), INTERVAL 7 DAY),  '自测上报',DATE_SUB(NOW(), INTERVAL 7 DAY)),

-- ============================================================
-- 李秀兰 (id=3)：血压监测数据
-- ============================================================
(3,'blood_pressure','150/95',DATE_SUB(NOW(), INTERVAL 120 DAY),'随访记录',DATE_SUB(NOW(), INTERVAL 120 DAY)),
(3,'blood_pressure','148/92',DATE_SUB(NOW(), INTERVAL 90 DAY), '门诊测量',DATE_SUB(NOW(), INTERVAL 90 DAY)),
(3,'blood_pressure','145/90',DATE_SUB(NOW(), INTERVAL 60 DAY), '随访记录',DATE_SUB(NOW(), INTERVAL 60 DAY)),
(3,'blood_pressure','142/88',DATE_SUB(NOW(), INTERVAL 30 DAY), '门诊测量',DATE_SUB(NOW(), INTERVAL 30 DAY)),
(3,'blood_pressure','140/86',DATE_SUB(NOW(), INTERVAL 10 DAY), '自测上报',DATE_SUB(NOW(), INTERVAL 10 DAY)),
-- 血糖
(3,'blood_glucose','8.1',DATE_SUB(NOW(), INTERVAL 115 DAY),'随访记录',DATE_SUB(NOW(), INTERVAL 115 DAY)),
(3,'blood_glucose','7.6',DATE_SUB(NOW(), INTERVAL 85 DAY), '门诊测量',DATE_SUB(NOW(), INTERVAL 85 DAY)),
(3,'blood_glucose','7.2',DATE_SUB(NOW(), INTERVAL 55 DAY), '随访记录',DATE_SUB(NOW(), INTERVAL 55 DAY)),
(3,'blood_glucose','6.8',DATE_SUB(NOW(), INTERVAL 25 DAY), '门诊测量',DATE_SUB(NOW(), INTERVAL 25 DAY)),
(3,'blood_glucose','7.0',DATE_SUB(NOW(), INTERVAL 8 DAY),  '自测上报',DATE_SUB(NOW(), INTERVAL 8 DAY)),

-- ============================================================
-- 王志强 (id=4)：血压监测数据
-- ============================================================
(4,'blood_pressure','125/80',DATE_SUB(NOW(), INTERVAL 90 DAY),'门诊测量',DATE_SUB(NOW(), INTERVAL 90 DAY)),
(4,'blood_pressure','122/78',DATE_SUB(NOW(), INTERVAL 60 DAY),'门诊测量',DATE_SUB(NOW(), INTERVAL 60 DAY)),
(4,'blood_pressure','120/76',DATE_SUB(NOW(), INTERVAL 30 DAY),'自测上报',DATE_SUB(NOW(), INTERVAL 30 DAY)),
(4,'weight','75.0',DATE_SUB(NOW(), INTERVAL 90 DAY),'体检',DATE_SUB(NOW(), INTERVAL 90 DAY)),
(4,'weight','74.5',DATE_SUB(NOW(), INTERVAL 30 DAY),'自测上报',DATE_SUB(NOW(), INTERVAL 30 DAY));

