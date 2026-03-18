-- V8: Mock数据 — 居民、医护人员、预约、就诊记录、签约、随访
-- 注意：sys_user.password = BCrypt('Abc@12345')
-- BCrypt hash for 'Abc@12345': $2a$10$abcdefghijklmnopqrstuvuABCDEFGHIJKLMNOPQRSTUVWXYZ1234

SET @pwd = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8a.9cGasOomDriwnju';

-- ============================================================
-- 1. 居民用户 (sys_resident + sys_user role=RESIDENT)
-- ============================================================
INSERT IGNORE INTO sys_user (id,username,password,name,phone,role,dept_name,status,created_at) VALUES
(1001,'R001',@pwd,'张伟','13800100001','RESIDENT','',1,NOW()-INTERVAL 180 DAY),
(1002,'R002',@pwd,'李敏','13800100002','RESIDENT','',1,NOW()-INTERVAL 160 DAY),
(1003,'R003',@pwd,'王芳','13800100003','RESIDENT','',1,NOW()-INTERVAL 140 DAY),
(1004,'R004',@pwd,'刘强','13800100004','RESIDENT','',1,NOW()-INTERVAL 120 DAY),
(1005,'R005',@pwd,'陈颖','13800100005','RESIDENT','',1,NOW()-INTERVAL 100 DAY),
(1006,'R006',@pwd,'杨阳','13800100006','RESIDENT','',1,NOW()-INTERVAL 90 DAY),
(1007,'R007',@pwd,'赵丽','13800100007','RESIDENT','',1,NOW()-INTERVAL 80 DAY),
(1008,'R008',@pwd,'孙磊','13800100008','RESIDENT','',1,NOW()-INTERVAL 70 DAY),
(1009,'R009',@pwd,'周婷','13800100009','RESIDENT','',1,NOW()-INTERVAL 60 DAY),
(1010,'R010',@pwd,'吴刚','13800100010','RESIDENT','',1,NOW()-INTERVAL 50 DAY);

INSERT IGNORE INTO sys_resident (id,name,gender,birth_date,phone,id_card,address,blood_type,status,created_at) VALUES
(1001,'张伟',1,'1985-03-12','13800100001','320101198503120011','南京市鼓楼区中山路1号','A',1,NOW()-INTERVAL 180 DAY),
(1002,'李敏',2,'1990-06-24','13800100002','320101199006240022','南京市玄武区珠江路2号','B',1,NOW()-INTERVAL 160 DAY),
(1003,'王芳',2,'1978-11-05','13800100003','320101197811050033','南京市秦淮区夫子庙3号','O',1,NOW()-INTERVAL 140 DAY),
(1004,'刘强',1,'1965-09-18','13800100004','320101196509180044','南京市建邺区河西4号','AB',1,NOW()-INTERVAL 120 DAY),
(1005,'陈颖',2,'1995-02-28','13800100005','320101199502280055','南京市栖霞区燕子矶5号','A',1,NOW()-INTERVAL 100 DAY),
(1006,'杨阳',1,'1972-07-14','13800100006','320101197207140066','南京市浦口区浦珠6号','B',1,NOW()-INTERVAL 90 DAY),
(1007,'赵丽',2,'1988-12-01','13800100007','320101198812010077','南京市雨花台区软件大道7号','O',1,NOW()-INTERVAL 80 DAY),
(1008,'孙磊',1,'1960-04-22','13800100008','320101196004220088','南京市江宁区东山8号','A',1,NOW()-INTERVAL 70 DAY),
(1009,'周婷',2,'1993-08-30','13800100009','320101199308300099','南京市六合区雄州9号','B',1,NOW()-INTERVAL 60 DAY),
(1010,'吴刚',1,'1980-01-15','13800100010','320101198001150100','南京市溧水区永阳10号','AB',1,NOW()-INTERVAL 50 DAY);

-- ============================================================
-- 2. 医生与护士 (sys_user role=DOCTOR/NURSE) — 与初始数据合并
-- ============================================================
INSERT IGNORE INTO sys_user (id,username,password,name,phone,role,dept_name,status,created_at) VALUES
(2001,'D001',@pwd,'李医生','13900200001','DOCTOR','全科门诊',1,NOW()-INTERVAL 365 DAY),
(2002,'D002',@pwd,'赵医生','13900200002','DOCTOR','慢病管理科',1,NOW()-INTERVAL 360 DAY),
(2003,'D003',@pwd,'王医生','13900200003','DOCTOR','儿科',1,NOW()-INTERVAL 355 DAY),
(3001,'N001',@pwd,'刘护士','13900300001','NURSE','全科门诊',1,NOW()-INTERVAL 365 DAY),
(3002,'N002',@pwd,'陈护士','13900300002','NURSE','疫苗接种科',1,NOW()-INTERVAL 360 DAY);

-- 简介
INSERT IGNORE INTO staff_profile (id,specialty,bio,title,years_exp) VALUES
(2001,'高血压、糖尿病、常见慢性病管理','李医生从事全科医学15年，擅长慢病综合管理与预防，深受患者信赖。','主治医师',15),
(2002,'糖尿病并发症、代谢综合征','赵医生专注慢性代谢性疾病，擅长糖尿病教育和个体化治疗方案制定。','副主任医师',20),
(2003,'儿童生长发育、儿科常见病','王医生从事儿科工作10年，擅长儿童健康管理和常见病诊疗。','主治医师',10);

-- ============================================================
-- 3. 排班 (schedule)
-- ============================================================
INSERT IGNORE INTO schedule (id,staff_id,staff_name,dept_code,dept_name,schedule_date,shift_type,available_slots,status,created_at) VALUES
(5001,2001,'李医生','GK','全科门诊',CURDATE(),'AM',15,1,NOW()),
(5002,2001,'李医生','GK','全科门诊',CURDATE(),'PM',15,1,NOW()),
(5003,2002,'赵医生','MB','慢病管理科',CURDATE(),'AM',10,1,NOW()),
(5004,2003,'王医生','EK','儿科',CURDATE(),'AM',12,1,NOW()),
(5005,2001,'李医生','GK','全科门诊',CURDATE()+INTERVAL 1 DAY,'AM',15,1,NOW()),
(5006,2002,'赵医生','MB','慢病管理科',CURDATE()+INTERVAL 1 DAY,'PM',10,1,NOW());

-- ============================================================
-- 4. 预约记录 (appointment)
-- ============================================================
INSERT IGNORE INTO appointment (id,resident_id,staff_id,dept_code,dept_name,staff_name,appt_date,visit_no,status,chief_complaint,created_at) VALUES
(6001,1001,2001,'GK','全科门诊','李医生',CURDATE()-INTERVAL 10 DAY,1,4,'头痛发热',NOW()-INTERVAL 10 DAY),
(6002,1002,2002,'MB','慢病管理科','赵医生',CURDATE()-INTERVAL 8 DAY,1,4,'血糖控制不佳',NOW()-INTERVAL 8 DAY),
(6003,1003,2001,'GK','全科门诊','李医生',CURDATE()-INTERVAL 5 DAY,1,4,'咳嗽3天',NOW()-INTERVAL 5 DAY),
(6004,1004,2003,'EK','儿科','王医生',CURDATE()-INTERVAL 3 DAY,1,4,'儿童体检',NOW()-INTERVAL 3 DAY),
(6005,1001,2001,'GK','全科门诊','李医生',CURDATE()-INTERVAL 1 DAY,1,2,'复诊头痛',NOW()-INTERVAL 1 DAY),
(6006,1005,2002,'MB','慢病管理科','赵医生',CURDATE(),1,1,'高血压随访',NOW()),
(6007,1006,2001,'GK','全科门诊','李医生',CURDATE(),2,1,'发热',NOW()),
(6008,1007,2001,'GK','全科门诊','李医生',CURDATE()+INTERVAL 1 DAY,1,1,'体检',NOW());

-- ============================================================
-- 5. 就诊记录 (visit_record)
-- ============================================================
INSERT IGNORE INTO visit_record (id,resident_id,staff_id,staff_name,dept_name,visit_date,chief_complaint,diagnosis,diagnosis_names,status,created_at) VALUES
(7001,1001,2001,'李医生','全科门诊',NOW()-INTERVAL 10 DAY,'头痛发热','J06.9','急性上呼吸道感染',2,NOW()-INTERVAL 10 DAY),
(7002,1002,2002,'赵医生','慢病管理科',NOW()-INTERVAL 8 DAY,'血糖控制不佳','E11.9','2型糖尿病',2,NOW()-INTERVAL 8 DAY),
(7003,1003,2001,'李医生','全科门诊',NOW()-INTERVAL 5 DAY,'咳嗽3天','J20.9','急性支气管炎',2,NOW()-INTERVAL 5 DAY),
(7004,1004,2003,'王医生','儿科',NOW()-INTERVAL 3 DAY,'儿童体检','Z00.1','儿童健康检查',2,NOW()-INTERVAL 3 DAY),
(7005,1001,2001,'李医生','全科门诊',NOW()-INTERVAL 1 DAY,'复诊头痛','G43.9','偏头痛',2,NOW()-INTERVAL 1 DAY);

-- ============================================================
-- 6. 家庭医生签约 (family_doctor_contract)
-- ============================================================
INSERT IGNORE INTO family_doctor_contract (id,resident_id,doctor_id,doctor_name,sign_date,expire_date,status,created_at) VALUES
(8001,1001,2001,'李医生',NOW()-INTERVAL 30 DAY,NOW()+INTERVAL 335 DAY,2,NOW()-INTERVAL 30 DAY),
(8002,1002,2002,'赵医生',NOW()-INTERVAL 60 DAY,NOW()+INTERVAL 305 DAY,2,NOW()-INTERVAL 60 DAY),
(8003,1003,2001,'李医生',NOW()-INTERVAL 90 DAY,NOW()+INTERVAL 275 DAY,2,NOW()-INTERVAL 90 DAY),
(8004,1005,2002,'赵医生',NOW()-INTERVAL 15 DAY,NOW()+INTERVAL 350 DAY,1,NOW()-INTERVAL 15 DAY); -- 待审核

-- ============================================================
-- 7. 随访计划 (follow_up_plan)
-- ============================================================
INSERT IGNORE INTO follow_up_plan (id,resident_id,staff_id,chronic_type,plan_date,status,conclusion,created_at) VALUES
(9001,1001,2001,'hypertension',CURDATE()-INTERVAL 5 DAY,2,1,NOW()-INTERVAL 5 DAY),
(9002,1002,2002,'diabetes',CURDATE()-INTERVAL 3 DAY,2,1,NOW()-INTERVAL 3 DAY),
(9003,1001,2001,'hypertension',CURDATE()+INTERVAL 25 DAY,1,NULL,NOW()),
(9004,1003,2001,'hypertension',CURDATE()-INTERVAL 10 DAY,3,NULL,NOW()-INTERVAL 10 DAY), -- 逾期
(9005,1002,2002,'diabetes',CURDATE()+INTERVAL 20 DAY,1,NULL,NOW());

-- ============================================================
-- 8. 疫苗接种预约 (vaccine_appointment)
-- ============================================================
INSERT IGNORE INTO vaccine_appointment (id,resident_id,vaccine_id,vaccine_name,dose_num,appt_date,status,created_at) VALUES
(10001,1005,1,'新冠疫苗',1,CURDATE(),1,NOW()-INTERVAL 2 DAY),
(10002,1006,2,'流感疫苗',1,CURDATE(),1,NOW()-INTERVAL 1 DAY),
(10003,1007,3,'乙肝疫苗',1,CURDATE()+INTERVAL 3 DAY,1,NOW()),
(10004,1001,1,'新冠疫苗',1,CURDATE()-INTERVAL 30 DAY,2,NOW()-INTERVAL 30 DAY), -- 已完成
(10005,1002,2,'流感疫苗',1,CURDATE()-INTERVAL 20 DAY,2,NOW()-INTERVAL 20 DAY); -- 已完成

-- ============================================================
-- 9. 健康档案 (health_record) — 关联居民ID
-- ============================================================
INSERT IGNORE INTO health_record (resident_id,allergy_history,past_medical_history,family_history,chronic_tags,emergency_contact,emergency_phone,created_at) VALUES
(1001,'青霉素过敏','无重大既往病史','父亲有高血压','高血压','张母','13800199001',NOW()-INTERVAL 180 DAY),
(1002,'无','2型糖尿病10年','母亲有糖尿病','糖尿病','李丈夫','13800199002',NOW()-INTERVAL 160 DAY),
(1003,'磺胺类过敏','无','无','高血压','王子','13800199003',NOW()-INTERVAL 140 DAY),
(1004,'无','冠心病、高血压','父亲心脏病','冠心病,高血压','刘女儿','13800199004',NOW()-INTERVAL 120 DAY),
(1005,'无','无','外祖母有糖尿病','','陈母','13800199005',NOW()-INTERVAL 100 DAY);
