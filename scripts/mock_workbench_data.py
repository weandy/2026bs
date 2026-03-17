"""
接诊工作台体验专用：一键灌入模拟候诊号源
用法：python scripts/mock_workbench_data.py
"""
import pymysql
import time
from datetime import datetime, timedelta

# ── 数据库连接（与 application-dev.yml 保持一致） ──
DB_CONFIG = {
    "host": "192.168.8.212",
    "port": 3306,
    "user": "root",
    "password": "mysql_eAk4W2",
    "database": "chp_resident",
    "charset": "utf8mb4",
}

# ── 配置项 ──
STAFF_ID = 1001     # 对应 chp_admin.staff 表中 DR001 李医生
SLOT_ID = 1
DEPT_CODE = "GP"
DEPT_NAME = "全科门诊"
STAFF_NAME = "系统模拟名"

# ── 模拟患者列表 ──
PATIENTS = [
    {"name": "李建国", "phone": "13810000001", "period": 1, "offset_min": 30},
    {"name": "王秀兰", "phone": "13810000002", "period": 1, "offset_min": 25},
    {"name": "张伟",   "phone": "13810000003", "period": 1, "offset_min": 20},
    {"name": "赵建平", "phone": "13810000004", "period": 2, "offset_min": 15},
    {"name": "孙丽华", "phone": "13810000005", "period": 2, "offset_min": 10},
    {"name": "陈强",   "phone": "13810000006", "period": 2, "offset_min": 5},
]


def main():
    conn = pymysql.connect(**DB_CONFIG)
    cursor = conn.cursor()
    now = datetime.now()
    today = now.strftime("%Y-%m-%d")

    # 1. 清理当日旧的测试数据
    del_sql = (
        "DELETE FROM appointment "
        "WHERE staff_id = %s AND appt_date = %s AND appt_no LIKE 'MOCK%%'"
    )
    cursor.execute(del_sql, (STAFF_ID, today))
    deleted = cursor.rowcount
    print(f"🗑️  已清理当日旧 MOCK 数据 {deleted} 条")

    # 2. 批量插入
    ins_sql = (
        "INSERT INTO appointment "
        "(appt_no, resident_id, slot_id, dept_code, dept_name, "
        " staff_id, staff_name, appt_date, time_period, "
        " patient_name, patient_phone, status, created_at, updated_at, is_deleted) "
        "VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)"
    )

    for i, p in enumerate(PATIENTS, start=1):
        appt_no = f"MOCK{int(time.time())}{i}"
        created_at = now - timedelta(minutes=p["offset_min"])
        cursor.execute(ins_sql, (
            appt_no,
            1000 + i,       # resident_id
            SLOT_ID,
            DEPT_CODE,
            DEPT_NAME,
            STAFF_ID,
            STAFF_NAME,
            today,
            p["period"],
            p["name"],
            p["phone"],
            1,              # status = 1 待叫号
            created_at,
            now,
            0,
        ))
        print(f"  ✅ [{i}] {p['name']}  {appt_no}")

    conn.commit()
    print(f"\n🎉 完成！已为医生 staffId={STAFF_ID} 插入 {len(PATIENTS)} 条候诊号源")
    print("   请前往 http://localhost:5173/medical/workbench 刷新查看")

    cursor.close()
    conn.close()


if __name__ == "__main__":
    main()
