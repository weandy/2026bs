-- ① family_doctor_contract
INSERT INTO chp_admin.family_doctor_contract(resident_id,team_name,doctor_id,doctor_name,nurse_id,nurse_name,contract_start,contract_end,service_package,status,created_at) VALUES
(1,    '李明家医团队',1001,'李明',2001,'刘护士','2025-01-15','2026-01-14','基础包',1,NOW()),
(1001, '李明家医团队',1001,'李明',2001,'刘护士','2025-02-20','2026-02-19','标准包',1,NOW()),
(1002, '李明家医团队',1001,'李明',2001,'刘护士','2025-03-10','2026-03-09','基础包',1,NOW()),
(1003, '陈华家医团队',1002,'陈华',NULL,NULL,'2025-01-05','2026-01-04','基础包',1,NOW()),
(2,    '陈华家医团队',1002,'陈华',NULL,NULL,'2025-04-18','2026-04-17','标准包',1,NOW()),
(3,    '赵磊家医团队',1003,'赵磊',NULL,NULL,'2025-05-22','2026-05-21','基础包',1,NOW()),
(4,    '赵磊家医团队',1003,'赵磊',NULL,NULL,'2025-06-30','2026-06-29','标准包',1,NOW()),
(5,    '李明家医团队',1001,'李明',2001,'刘护士','2024-08-01','2025-07-31','基础包',0,NOW()),
(6,    '陈华家医团队',1002,'陈华',NULL,NULL,'2024-09-15','2025-09-14','基础包',0,NOW()),
(7,    '赵磊家医团队',1003,'赵磊',NULL,NULL,'2024-10-20','2025-10-19','基础包',0,NOW()),
(1001, '陈华家医团队',1002,'陈华',NULL,NULL,'2024-02-01','2025-01-31','基础包',0,NOW()),
(1002, '赵磊家医团队',1003,'赵磊',NULL,NULL,'2024-03-15','2025-03-14','基础包',0,NOW()),
(1003, '李明家医团队',1001,'李明',2001,'刘护士','2024-04-01','2025-03-31','标准包',0,NOW()),
(1,    '陈华家医团队',1002,'陈华',NULL,NULL,'2023-12-01','2024-11-30','基础包',0,NOW()),
(2,    '李明家医团队',1001,'李明',2001,'刘护士','2023-11-01','2024-10-31','基础包',0,NOW());

-- ② drug_stock_log（op_type 1=入库 0=出库，direction 1=入 0=出）
INSERT INTO chp_admin.drug_stock_log(drug_id,drug_name,batch_no,op_type,quantity,direction,balance,operator_id,operator_name,op_time,remark) VALUES
(1,'阿莫西林胶囊','BN20250101',1,200,1,200,1001,'李明', DATE_SUB(NOW(),INTERVAL 30 DAY),'常规采购入库'),
(2,'布洛芬缓释胶囊','BN20250102',1,150,1,150,1001,'李明', DATE_SUB(NOW(),INTERVAL 28 DAY),'常规采购入库'),
(3,'二甲双胍片','BN20250103',1,300,1,300,1002,'陈华', DATE_SUB(NOW(),INTERVAL 25 DAY),'慢病专项采购'),
(4,'苯磺酸氨氯地平','BN20250104',1,250,1,250,1002,'陈华', DATE_SUB(NOW(),INTERVAL 22 DAY),'高血压用药补货'),
(5,'阿托伐他汀钙片','BN20250105',1,180,1,180,1003,'赵磊', DATE_SUB(NOW(),INTERVAL 20 DAY),'降脂药季度采购'),
(6,'氯雷他定片','BN20250106',1,120,1,120,1001,'李明', DATE_SUB(NOW(),INTERVAL 18 DAY),'常规采购入库'),
(7,'对乙酰氨基酚片','BN20250107',1,400,1,400,1002,'陈华', DATE_SUB(NOW(),INTERVAL 15 DAY),'退热药大批补货'),
(8,'头孢克肟胶囊','BN20250108',1,100,1,100,1003,'赵磊', DATE_SUB(NOW(),INTERVAL 12 DAY),'常规采购入库'),
(9,'奥美拉唑肠溶片','BN20250109',1,200,1,200,1001,'李明', DATE_SUB(NOW(),INTERVAL 10 DAY),'消化道用药补货'),
(10,'利格列汀片','BN20250110',1,90,1,90,1002,'陈华', DATE_SUB(NOW(),INTERVAL 8 DAY),'糖尿病新型药采购'),
(1,'阿莫西林胶囊','BN20250101',0,5,0,195,1001,'李明', DATE_SUB(NOW(),INTERVAL 14 DAY),'处方出库: RX20260315001'),
(2,'布洛芬缓释胶囊','BN20250102',0,3,0,147,1001,'李明', DATE_SUB(NOW(),INTERVAL 14 DAY),'处方出库: RX20260315001'),
(3,'二甲双胍片','BN20250103',0,60,0,240,1002,'陈华', DATE_SUB(NOW(),INTERVAL 13 DAY),'处方出库: RX20260316001'),
(4,'苯磺酸氨氯地平','BN20250104',0,30,0,220,1002,'陈华', DATE_SUB(NOW(),INTERVAL 13 DAY),'处方出库: RX20260316002'),
(5,'阿托伐他汀钙片','BN20250105',0,14,0,166,1003,'赵磊', DATE_SUB(NOW(),INTERVAL 12 DAY),'处方出库: RX20260317001'),
(7,'对乙酰氨基酚片','BN20250107',0,10,0,390,1003,'赵磊', DATE_SUB(NOW(),INTERVAL 11 DAY),'处方出库: RX20260317002'),
(9,'奥美拉唑肠溶片','BN20250109',0,14,0,186,1001,'李明', DATE_SUB(NOW(),INTERVAL 5 DAY),'处方出库: RX20260318001'),
(6,'氯雷他定片','BN20250106',0,7,0,113,1002,'陈华', DATE_SUB(NOW(),INTERVAL 4 DAY),'处方出库: RX20260318002'),
(1,'阿莫西林胶囊','BN20250201',1,100,1,295,1001,'李明', DATE_SUB(NOW(),INTERVAL 3 DAY),'紧急补货入库'),
(8,'头孢克肟胶囊','BN20250108',0,6,0,94,1003,'赵磊', DATE_SUB(NOW(),INTERVAL 2 DAY),'处方出库: RX20260318003'),
(3,'二甲双胍片','BN20250103',0,30,0,210,1001,'李明', DATE_SUB(NOW(),INTERVAL 1 DAY),'处方出库: RX20260318004'),
(10,'利格列汀片','BN20250110',0,7,0,83,1002,'陈华', NOW(),'处方出库: RX20260318005'),
(2,'布洛芬缓释胶囊','BN20250202',1,80,1,224,1001,'李明', NOW(),'月度常规补货'),
(4,'苯磺酸氨氯地平','BN20250104',0,28,0,192,1003,'赵磊', NOW(),'处方出库: RX20260318006'),
(5,'阿托伐他汀钙片','BN20250105',0,21,0,145,1001,'李明', NOW(),'处方出库: RX20260318007');

-- ③ vaccine_stock_log
INSERT INTO chp_admin.vaccine_stock_log(vaccine_id,vaccine_name,batch_no,op_type,quantity,balance,operator_id,operator_name,op_time,remark) VALUES
(1,'流感疫苗(四价)','VBN20250101',1,100,100,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 40 DAY),'春季专项采购入库'),
(2,'乙肝疫苗','VBN20250102',1,80,80,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 38 DAY),'常规采购入库'),
(3,'新冠疫苗(二价)','VBN20250103',1,150,150,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 35 DAY),'政府统一配发入库'),
(4,'肺炎球菌疫苗','VBN20250104',1,60,60,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 30 DAY),'老年人专项采购'),
(5,'带状疱疹疫苗','VBN20250105',1,40,40,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 28 DAY),'常规采购入库'),
(1,'流感疫苗(四价)','VBN20250101',0,5,95,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 20 DAY),'接种出库 居民ID:1001'),
(1,'流感疫苗(四价)','VBN20250101',0,3,92,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 18 DAY),'接种出库 居民ID:1002'),
(2,'乙肝疫苗','VBN20250102',0,2,78,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 17 DAY),'接种出库 居民ID:1003'),
(3,'新冠疫苗(二价)','VBN20250103',0,4,146,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 15 DAY),'接种出库 居民ID:1001'),
(4,'肺炎球菌疫苗','VBN20250104',0,3,57,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 14 DAY),'接种出库 居民ID:1'),
(5,'带状疱疹疫苗','VBN20250105',0,2,38,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 12 DAY),'接种出库 居民ID:1002'),
(1,'流感疫苗(四价)','VBN20250201',1,50,142,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 10 DAY),'二次补货入库'),
(2,'乙肝疫苗','VBN20250102',0,3,75,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 9 DAY),'接种出库 居民ID:1003'),
(3,'新冠疫苗(二价)','VBN20250103',0,5,141,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 7 DAY),'接种出库 居民ID:1002'),
(6,'HPV疫苗(九价)','VBN20250106',1,30,30,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 6 DAY),'新品采购入库'),
(4,'肺炎球菌疫苗','VBN20250104',0,2,55,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 5 DAY),'接种出库 居民ID:1001'),
(6,'HPV疫苗(九价)','VBN20250106',0,3,27,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 3 DAY),'接种出库 居民ID:1003'),
(1,'流感疫苗(四价)','VBN20250201',0,6,136,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 2 DAY),'接种出库 居民ID:1001'),
(5,'带状疱疹疫苗','VBN20250105',0,1,37,2001,'刘护士',DATE_SUB(NOW(),INTERVAL 1 DAY),'接种出库 居民ID:1'),
(3,'新冠疫苗(二价)','VBN20250103',0,2,139,2001,'刘护士',NOW(),'接种出库 居民ID:1002');

-- ④ family_member
INSERT INTO chp_resident.family_member(owner_id,member_name,member_phone,relation,id_card,status,created_at) VALUES
(1001,'王建国','13811001100','父亲','330102196005120015',1,NOW()),
(1001,'李秀兰','13811001101','母亲','330102196308250025',1,NOW()),
(1001,'王小明',NULL,'子女','330102201503100035',1,NOW()),
(1002,'张国庆','13911002200','配偶','330102198510010045',1,NOW()),
(1002,'张欣悦',NULL,'子女','330102201807220055',1,NOW()),
(1003,'刘大力','13711003300','父亲','330102195512180065',1,NOW()),
(1003,'陈美华','13711003301','母亲','330102195803300075',1,NOW()),
(1,'张国华','13611004400','配偶','330102197809150085',1,NOW()),
(1,'张小雨',NULL,'子女','330102201006060095',1,NOW()),
(2,'刘建设','13511005500','父亲','330102196011200105',1,NOW());

-- ⑤ visit_evaluation（先确认列名后精准插入）
INSERT INTO chp_resident.visit_evaluation(visit_id,resident_id,staff_id,score,comment,created_at)
SELECT id,resident_id,staff_id,
  FLOOR(4+RAND()),
  ELT(FLOOR(1+RAND()*5),'医生态度非常好，解释清晰，非常满意！','服务很专业，诊断准确，下次还会来','医生耐心细致，给我解答了很多疑问','整体就诊体验不错，医生很有责任心','服务态度很好，环境干净整洁，好评！'),
  DATE_ADD(visit_date, INTERVAL FLOOR(1+RAND()*3) DAY)
FROM chp_resident.visit_record
ORDER BY id LIMIT 18;

-- ⑥ follow_up_plan
INSERT INTO chp_resident.follow_up_plan(resident_id,disease_name,risk_level,next_follow_up_date,follow_up_cycle_days,status,created_by,created_at) VALUES
(1001,'高血压','HIGH', DATE_ADD(CURDATE(),INTERVAL 7 DAY), 90,0,1001,NOW()),
(1002,'2型糖尿病','MEDIUM',DATE_ADD(CURDATE(),INTERVAL 3 DAY), 30,0,1001,NOW()),
(1003,'慢阻肺','HIGH', DATE_ADD(CURDATE(),INTERVAL 14 DAY),90,0,1002,NOW()),
(1,'冠心病','HIGH', DATE_ADD(CURDATE(),INTERVAL 5 DAY), 60,0,1002,NOW()),
(2,'高血脂','LOW', DATE_ADD(CURDATE(),INTERVAL 21 DAY),180,0,1003,NOW()),
(3,'骨质疏松','LOW', DATE_ADD(CURDATE(),INTERVAL 10 DAY),90,0,1003,NOW()),
(1001,'高血压','HIGH', DATE_SUB(CURDATE(),INTERVAL 7 DAY), 90,1,1001,NOW()),
(1002,'2型糖尿病','MEDIUM',DATE_SUB(CURDATE(),INTERVAL 14 DAY),30,1,1002,NOW()),
(1003,'慢阻肺','HIGH', DATE_SUB(CURDATE(),INTERVAL 30 DAY),90,1,1003,NOW()),
(1,'冠心病','HIGH', DATE_SUB(CURDATE(),INTERVAL 3 DAY), 60,2,1001,NOW()),
(4,'骨质疏松','LOW', DATE_ADD(CURDATE(),INTERVAL 28 DAY),90,0,1002,NOW());
