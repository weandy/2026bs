import pymysql
import json
from datetime import datetime, timedelta
import random

def seed_data():
    conn = pymysql.connect(host='192.168.8.212', port=3306, user='root', password='mysql_eAk4W2', db='chp_resident')
    cursor = conn.cursor(pymysql.cursors.DictCursor)
    try:
        # Find resident Id for 13800000001
        cursor.execute("SELECT id FROM resident WHERE phone='13800000001'")
        res = cursor.fetchone()
        if not res:
            print("Resident 13800000001 not found!")
            return
        r_id = res['id']
        
        # 1. UPSERT HealthRecord
        cursor.execute("SELECT id FROM health_record WHERE resident_id=%s", (r_id,))
        hr = cursor.fetchone()
        if not hr:
            cursor.execute("""
                INSERT INTO health_record 
                (resident_id, allergy_history, past_medical_history, family_history, chronic_tags, emergency_contact, emergency_phone, created_at, updated_at, is_deleted) 
                VALUES (%s, '青霉素过敏', '2015 年确诊高血压，2018 年确诊糖尿病。', '父亲有冠心病病史。', 'hypertension,diabetes', '家属张三', '13900000002', NOW(), NOW(), 0)
            """, (r_id,))
        else:
            cursor.execute("""
                UPDATE health_record SET 
                    allergy_history='青霉素过敏', 
                    past_medical_history='2015 年确诊高血压，2018 年确诊糖尿病。', 
                    family_history='父亲有冠心病病史。', 
                    chronic_tags='hypertension,diabetes'
                WHERE resident_id=%s
            """, (r_id,))
            
        # 2. Add realistic VisitRecords (last 12 months)
        # Clear existing for fresh start
        cursor.execute("DELETE FROM visit_record WHERE resident_id=%s", (r_id,))
        
        now = datetime.now()
        for i in range(12, 0, -1):
            visit_date = now - timedelta(days=i*30 + random.randint(-5, 5))
            
            # Generate fluctuating vitals
            sys = random.randint(125, 145)
            dia = random.randint(80, 95)
            glu = round(random.uniform(5.8, 7.5), 1)
            wht = round(random.uniform(70.0, 75.0), 1)
            
            pexam = json.dumps({
                "bloodPressureSys": sys,
                "bloodPressureDia": dia,
                "bloodGlucose": glu,
                "weight": wht,
                "temperature": 36.5,
                "pulse": random.randint(70, 85)
            })
            
            cursor.execute("""
                INSERT INTO visit_record 
                (appointment_id, resident_id, staff_id, staff_name, dept_code, dept_name, visit_date, chief_complaint, present_illness, physical_exam, diagnosis_names, medical_advice, created_at, updated_at, is_deleted)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, 0)
            """, (
                1000 + i, r_id, 1, '李医生', 'internal', '全科门诊', visit_date.strftime('%Y-%m-%d'), 
                '常规慢性病随访、复查开药', '高血压、糖尿病稳定期', pexam, '高血压病, Ⅱ型糖尿病', 
                '继续当前方案用药，注意低盐低脂饮食。', 
                visit_date.strftime('%Y-%m-%d %H:%M:%S'), visit_date.strftime('%Y-%m-%d %H:%M:%S')
            ))
            
        # 3. Add Message
        cursor.execute("DELETE FROM message WHERE resident_id=%s", (r_id,))
        cursor.execute("""
            INSERT INTO message (resident_id, title, content, msg_type, is_read, created_at)
            VALUES 
            (%s, '高血压随访提醒', '您近期的血压记录存在波动，请记得按时服用降压药，并于本周五前来全科门诊复查。', 'followup', 0, NOW()),
            (%s, '系统升级通知', '社区卫生服务中心平台将于今晚进行系统升级，期间可能影响部分线上业务办理，敬请谅解。', 'system', 0, DATE_SUB(NOW(), INTERVAL 1 DAY))
        """, (r_id, r_id))
            
        conn.commit()
        print("Mock data injected successfully!")
        
    except Exception as e:
        print("Error:", e)
        conn.rollback()
    finally:
        conn.close()

if __name__ == '__main__':
    seed_data()
