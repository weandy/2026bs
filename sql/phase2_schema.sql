-- =============================================
-- Phase 2 新增表 — 社区卫生平台功能扩展
-- =============================================

-- ========== chp_resident 库 ==========
USE chp_resident;

-- 居民健康指标自录入
CREATE TABLE IF NOT EXISTS health_vital (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  resident_id BIGINT NOT NULL,
  vital_type VARCHAR(32) NOT NULL COMMENT 'blood_pressure|blood_glucose|weight|heart_rate|step_count',
  vital_value VARCHAR(64) NOT NULL,
  measure_time DATETIME NOT NULL,
  note VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_resident_type (resident_id, vital_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='居民健康指标记录';

-- 家庭成员
CREATE TABLE IF NOT EXISTS family_member (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  owner_id BIGINT NOT NULL COMMENT '主账户居民ID',
  member_name VARCHAR(64) NOT NULL,
  member_phone VARCHAR(20),
  relation VARCHAR(20) NOT NULL COMMENT 'spouse|child|parent|other',
  id_card VARCHAR(18),
  linked_resident_id BIGINT COMMENT '若也是注册居民则关联',
  status TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭成员';

-- 就诊评价
CREATE TABLE IF NOT EXISTS visit_evaluation (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  visit_id BIGINT NOT NULL,
  resident_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  score TINYINT NOT NULL COMMENT '1-5',
  comment VARCHAR(500),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_visit (visit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊评价';

-- ========== chp_admin 库 ==========
USE chp_admin;

-- 健康资讯文章
CREATE TABLE IF NOT EXISTS article (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(128) NOT NULL,
  cover_url VARCHAR(255),
  content TEXT NOT NULL,
  category VARCHAR(32) COMMENT 'chronic|diet|exercise|vaccine|general',
  author_name VARCHAR(64),
  view_count INT DEFAULT 0,
  is_published TINYINT DEFAULT 0,
  published_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康资讯文章';

-- 病历模板
CREATE TABLE IF NOT EXISTS medical_template (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  template_name VARCHAR(128) NOT NULL,
  category VARCHAR(32) COMMENT 'hypertension|diabetes|cold|gastritis|custom',
  chief_complaint TEXT,
  diagnosis TEXT,
  treatment_plan TEXT,
  creator_id BIGINT,
  is_public TINYINT DEFAULT 1,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='病历模板';

-- 转诊单
CREATE TABLE IF NOT EXISTS referral (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  visit_id BIGINT,
  resident_id BIGINT NOT NULL,
  from_staff_id BIGINT NOT NULL,
  from_dept_code VARCHAR(20),
  to_hospital VARCHAR(128) NOT NULL,
  to_dept VARCHAR(64),
  reason TEXT NOT NULL,
  diagnosis VARCHAR(255),
  status TINYINT DEFAULT 1 COMMENT '1=已开具 2=已到诊 3=已回转',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转诊单';

-- 会诊请求
CREATE TABLE IF NOT EXISTS consultation_request (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  visit_id BIGINT,
  requester_id BIGINT NOT NULL,
  requester_dept VARCHAR(64),
  target_dept_code VARCHAR(20) NOT NULL,
  target_dept_name VARCHAR(64),
  reason TEXT NOT NULL,
  status TINYINT DEFAULT 1 COMMENT '1=待处理 2=已接受 3=已完成 4=已拒绝',
  response_note TEXT,
  responder_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  responded_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会诊请求';

-- 就诊附件
CREATE TABLE IF NOT EXISTS visit_attachment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  visit_id BIGINT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_url VARCHAR(512) NOT NULL,
  file_type VARCHAR(20) COMMENT 'image|pdf|report',
  file_size BIGINT,
  uploaded_by BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='就诊附件';

-- 家庭医生签约
CREATE TABLE IF NOT EXISTS family_doctor_contract (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  resident_id BIGINT NOT NULL,
  team_name VARCHAR(64),
  doctor_id BIGINT NOT NULL,
  doctor_name VARCHAR(64),
  nurse_id BIGINT,
  nurse_name VARCHAR(64),
  contract_start DATE NOT NULL,
  contract_end DATE NOT NULL,
  service_package VARCHAR(64) COMMENT 'basic|chronic|elder',
  status TINYINT DEFAULT 1 COMMENT '1=生效 2=到期 3=解约',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭医生签约';

-- ========== 种子数据 ==========

-- 病历模板预设
INSERT INTO medical_template (template_name, category, chief_complaint, diagnosis, treatment_plan, is_public) VALUES
('高血压常规复查', 'hypertension', '头晕、乏力反复发作1周', 'I10 原发性高血压', '1.继续口服降压药\n2.低盐低脂饮食\n3.监测血压\n4.2周后复诊', 1),
('2型糖尿病随访', 'diabetes', '口渴、多尿加重3天', 'E11 2型糖尿病', '1.调整降糖方案\n2.控制饮食\n3.适量运动\n4.定期监测血糖', 1),
('感冒（上呼吸道感染）', 'cold', '鼻塞、流涕、咽痛2天，伴低热', 'J06.9 急性上呼吸道感染', '1.对症治疗\n2.多饮水\n3.注意休息\n4.如症状加重及时复诊', 1),
('慢性胃炎', 'gastritis', '上腹隐痛、反酸1月', 'K29.5 慢性胃炎', '1.质子泵抑制剂\n2.规律饮食\n3.忌辛辣刺激\n4.1月后复诊', 1);
