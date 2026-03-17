-- 种子数据（密码统一 123456 → BCrypt: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi）

-- ========== chp_admin ==========
USE chp_admin;

-- 角色权限（doctor=2, nurse=3, admin=4）
INSERT INTO role_module_perm (role_id, module_code, can_view, can_create, can_edit, can_delete, can_export) VALUES
(2,'consultation',1,1,1,0,0),(2,'prescription',1,1,0,0,0),(2,'health_record',1,1,1,0,0),
(2,'follow_up',1,1,1,0,0),(2,'vaccine_admin',1,0,0,0,0),(2,'public_health',1,1,0,0,0),
(2,'schedule_view',1,0,0,0,0),(2,'appointment',1,0,0,0,0),
(3,'pharmacy',1,1,1,0,0),(3,'health_record',1,1,0,0,0),(3,'vaccine_admin',1,1,1,0,0),
(3,'public_health',1,1,1,0,0),(3,'schedule_view',1,0,0,0,0),(3,'follow_up',1,1,1,0,0),
(4,'user_mgmt',1,1,1,1,1),(4,'schedule_mgmt',1,1,1,1,1),(4,'drug_mgmt',1,1,1,1,1),
(4,'vaccine_stock',1,1,1,1,1),(4,'report',1,0,0,0,1),(4,'audit_log',1,0,0,0,0),
(4,'sys_config',1,0,1,0,0),(4,'consultation',1,1,1,1,1),(4,'prescription',1,1,1,1,1),
(4,'pharmacy',1,1,1,1,1),(4,'health_record',1,1,1,1,1),(4,'follow_up',1,1,1,1,1);

-- 医护账号
INSERT INTO staff (username,name,password,phone,gender,dept_code,dept_name,job_title,introduction,role_id,status) VALUES
('DR001','李明华','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','13600000001',1,'QKMZ','全科门诊','主任医师','擅长高血压糖尿病管理',2,1),
('DR002','王秀英','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','13600000002',2,'QKMZ','全科门诊','主治医师','擅长呼吸系统疾病',2,1),
('DR003','张伟强','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','13600000003',1,'KQK','口腔科','副主任医师','擅长口腔修复',2,1),
('DR004','陈丽芳','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','13600000004',2,'FYBJ','妇幼保健科','主治医师','擅长妇产科',2,1),
('NS001','刘晓燕','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','13600000005',2,'QKMZ','全科门诊','主管护师',NULL,3,1),
('NS002','赵小红','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','13600000006',2,'GGWS','公共卫生科','护师',NULL,3,1),
('ADMIN','系统管理员','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','13600000099',1,NULL,NULL,NULL,NULL,4,1)
ON DUPLICATE KEY UPDATE username=VALUES(username);  -- AD001 实际由 init 脚本创建

-- 药品（10种）
INSERT INTO drug (drug_code,generic_name,trade_name,spec,dosage_form,unit,pinyin_code,manufacturer,alert_qty) VALUES
('D001','苯磺酸氨氯地平片','络活喜','5mg×28片','片剂','盒','BSAALDP','辉瑞',20),
('D002','二甲双胍缓释片','格华止','500mg×30片','片剂','盒','EJSGHRP','施贵宝',20),
('D003','阿莫西林胶囊',NULL,'500mg×24粒','胶囊','盒','AMXLJN','石药集团',30),
('D004','布洛芬缓释胶囊','芬必得','300mg×20粒','胶囊','盒','BLFHRJN','中美史克',30),
('D005','复方甘草片',NULL,'100片/瓶','片剂','瓶','FFGCP','华润三九',50),
('D006','氯沙坦钾片','科素亚','50mg×7片','片剂','盒','LSDKP','默沙东',20),
('D007','阿托伐他汀钙片','立普妥','20mg×7片','片剂','盒','ATFTDGP','辉瑞',20),
('D008','盐酸二甲双胍片',NULL,'250mg×100片','片剂','瓶','YSEJSGP','施贵宝',20),
('D009','头孢克洛缓释片',NULL,'375mg×6片','片剂','盒','TBKLHRP','礼来',30),
('D010','奥美拉唑肠溶胶囊','洛赛克','20mg×14粒','胶囊','盒','AMLCZJN','阿斯利康',20);

-- 药品库存（使用子查询关联 drug_code，避免 AUTO_INCREMENT 偏移）
INSERT INTO drug_stock (drug_id,batch_no,quantity,expire_date,supplier,purchase_price,inbound_at,inbound_by) VALUES
((SELECT id FROM drug WHERE drug_code='D001'),'20250101-A',200,'2027-01-01','国药控股',15.00,'2025-01-15',7),
((SELECT id FROM drug WHERE drug_code='D002'),'20250201-A',150,'2027-02-01','华润医药',8.00,'2025-02-10',7),
((SELECT id FROM drug WHERE drug_code='D003'),'20250301-A',300,'2026-09-30','石药直供',3.50,'2025-03-01',7),
((SELECT id FROM drug WHERE drug_code='D004'),'20250101-A',200,'2027-01-01','中美史克',12.00,'2025-01-20',7),
((SELECT id FROM drug WHERE drug_code='D005'),'20250401-A',500,'2027-04-01','华润三九',2.00,'2025-04-01',7),
((SELECT id FROM drug WHERE drug_code='D006'),'20250301-A',100,'2027-03-01','默沙东',22.00,'2025-03-15',7),
((SELECT id FROM drug WHERE drug_code='D007'),'20250201-A',120,'2027-02-01','辉瑞',18.00,'2025-02-20',7),
((SELECT id FROM drug WHERE drug_code='D008'),'20250501-A',200,'2027-05-01','施贵宝',5.00,'2025-05-01',7),
((SELECT id FROM drug WHERE drug_code='D009'),'20250301-A',180,'2026-12-01','礼来',15.00,'2025-03-10',7),
((SELECT id FROM drug WHERE drug_code='D010'),'20250401-A',150,'2027-04-01','阿斯利康',10.00,'2025-04-15',7);

-- 疫苗（5种）
INSERT INTO vaccine (vaccine_code,vaccine_name,target_disease,applicable_age,total_doses,dose_interval,storage_req,vaccine_type,price,alert_qty) VALUES
('VAC001','流感疫苗(四价)','流感','6月龄以上',1,NULL,'2-8℃',2,88.00,10),
('VAC002','乙肝疫苗','乙肝','新生儿及成人',3,'0-1-6月','2-8℃',1,0.00,10),
('VAC003','23价肺炎疫苗','肺炎球菌','2岁以上',1,NULL,'2-8℃',2,198.00,10),
('VAC004','水痘疫苗','水痘','1岁以上',2,'间隔3月','2-8℃',2,150.00,10),
('VAC005','带状疱疹疫苗','带状疱疹','50岁以上',2,'间隔2-6月','2-8℃',2,1580.00,5);

INSERT INTO vaccine_stock (vaccine_id,batch_no,quantity,expire_date,inbound_at,inbound_by) VALUES
(1,'FLU-2025A',200,'2026-06-30','2025-09-01',7),
(2,'HBV-2025A',300,'2027-12-31','2025-01-15',7),
(3,'PCV-2025A',80,'2027-06-30','2025-03-01',7),
(4,'VZV-2025A',100,'2027-03-31','2025-02-01',7),
(5,'HZV-2025A',30,'2027-09-30','2025-04-01',7);

-- 排班（未来7天）
INSERT INTO schedule (staff_id,staff_name,dept_code,dept_name,schedule_date,created_by) VALUES
(1,'李明华','QKMZ','全科门诊',CURDATE(),7),(1,'李明华','QKMZ','全科门诊',CURDATE()+INTERVAL 1 DAY,7),
(1,'李明华','QKMZ','全科门诊',CURDATE()+INTERVAL 2 DAY,7),(1,'李明华','QKMZ','全科门诊',CURDATE()+INTERVAL 3 DAY,7),
(2,'王秀英','QKMZ','全科门诊',CURDATE(),7),(2,'王秀英','QKMZ','全科门诊',CURDATE()+INTERVAL 1 DAY,7),
(3,'张伟强','KQK','口腔科',CURDATE(),7),(3,'张伟强','KQK','口腔科',CURDATE()+INTERVAL 2 DAY,7);

INSERT INTO schedule_slot (schedule_id,time_period,total_quota,remaining) SELECT id,1,20,20 FROM schedule;
INSERT INTO schedule_slot (schedule_id,time_period,total_quota,remaining) SELECT id,2,15,15 FROM schedule;

-- ICD-10（20条常用）
INSERT INTO icd_code (code,name_cn,category,pinyin_code) VALUES
('I10','原发性高血压','循环系统','YFXGXY'),('E11','2型糖尿病','内分泌','2XTNB'),
('I25.1','冠心病','循环系统','GXB'),('J06.9','急性上呼吸道感染','呼吸系统','JXSHXDGR'),
('J18.9','肺炎','呼吸系统','FY'),('K29.5','慢性胃炎','消化系统','MXWY'),
('M54.5','腰痛','肌肉骨骼','YT'),('E78.5','高脂血症','内分泌','GZXZ'),
('R50','发热','症状体征','FR'),('R05','咳嗽','症状体征','KS'),
('J44','慢阻肺','呼吸系统','MZFJ'),('F32','抑郁发作','精神科','YYFZ'),
('I63','脑梗死','循环系统','NGS'),('N39.0','尿路感染','泌尿系统','NLGR'),
('L30.9','皮炎','皮肤科','PY'),('Z00','一般健康检查','预防','YBJKJC'),
('Z23','预防接种','预防','YFJZ'),('J45','哮喘','呼吸系统','XC'),
('K21','胃食管反流','消化系统','WSGFL'),('E03','甲减','内分泌','JJ');

-- ========== chp_resident ==========
USE chp_resident;

INSERT INTO resident (id_card,name,gender,birth_date,phone,password,blood_type,address,status) VALUES
('110101196503150011','张建国',1,'1965-03-15','13800000001','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','A','海淀区学院路',1),
('110101197208200022','李秀兰',2,'1972-08-20','13800000002','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','B','海淀区中关村',1),
('110101198511030033','王志强',1,'1985-11-03','13800000003','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','O','朝阳区望京',1),
('110101199002140044','赵小燕',2,'1990-02-14','13800000004','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','AB','西城区德胜',1),
('110101200106250055','陈浩然',1,'2001-06-25','13800000005','$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi','A','海淀区花园路',1);

INSERT INTO health_record (resident_id,allergy_history,past_medical_history,chronic_tags,emergency_contact,emergency_phone,created_by) VALUES
(1,'[{"type":"drug","desc":"青霉素过敏"}]','[{"disease":"高血压","year":"2015"},{"disease":"糖尿病","year":"2018"}]','hypertension,diabetes','李秀兰','13800000002',1),
(2,'[]','[{"disease":"慢性胃炎","year":"2020"}]',NULL,'张建国','13800000001',1),
(3,'[{"type":"food","desc":"海鲜过敏"}]','[]',NULL,'赵小燕','13800000004',2),
(4,'[]','[]',NULL,'王志强','13800000003',2),
(5,'[]','[]',NULL,'张建国','13800000001',1);

-- ========== 数据字典 ==========
INSERT INTO sys_dict (dict_type, dict_code, dict_name, sort_order, status) VALUES
-- 疾病码
('disease','I10','原发性高血压',1,1),('disease','E11','2型糖尿病',2,1),
('disease','I25','慢性缺血性心脏病',3,1),('disease','J44','慢性阻塞性肺疾病',4,1),
('disease','K29','慢性胃炎',5,1),('disease','N18','慢性肾脏病',6,1),
-- 药品码
('drug','D001','苯磺酸氨氯地平片',1,1),('drug','D002','盐酸二甲双胍片',2,1),
('drug','D003','阿司匹林肠溶片',3,1),('drug','D004','阿托伐他汀钙片',4,1),
('drug','D005','头孢克肟胶囊',5,1),
-- 疫苗码
('vaccine','V001','乙肝疫苗',1,1),('vaccine','V002','新冠灭活疫苗',2,1),
('vaccine','V003','流感疫苗',3,1),('vaccine','V004','肺炎球菌疫苗',4,1),
('vaccine','V005','水痘疫苗',5,1),('vaccine','V006','HPV疫苗',6,1),
-- 检验项码
('exam','BLD001','血常规',1,1),('exam','BLD002','生化全套',2,1),
('exam','BLD003','尿常规',3,1),('exam','BLD004','空腹血糖',4,1),
('exam','BLD005','糖化血红蛋白',5,1),('exam','IMG001','胸部X光',6,1);
