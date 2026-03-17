"""
SQL 文件执行脚本
用法：python tools/db/run_sql_file.py <sql_file_path>
"""
import json, os, sys

CONFIG_PATH = os.path.join(os.path.dirname(__file__), "db_config.json")

def load_config():
    with open(CONFIG_PATH, "r") as f: return json.load(f)

def run_sql_file(filepath):
    cfg = load_config()
    if not os.path.exists(filepath):
        print(f"FAIL: 文件不存在 - {filepath}")
        return False
    with open(filepath, "r", encoding="utf-8") as f:
        sql_content = f.read()
    try:
        import pymysql
        conn = pymysql.connect(host=cfg["host"], port=cfg["port"], user=cfg["user"], password=cfg["password"],
                               charset="utf8mb4", autocommit=True)
    except ImportError:
        import mysql.connector
        conn = mysql.connector.connect(host=cfg["host"], port=cfg["port"], user=cfg["user"], password=cfg["password"],
                                       charset="utf8mb4", autocommit=True)
    cursor = conn.cursor()
    # 按分号分割执行（跳过空语句）
    statements = [s.strip() for s in sql_content.split(";") if s.strip()]
    success, failed = 0, 0
    for stmt in statements:
        if not stmt or stmt.startswith("--"): continue
        try:
            cursor.execute(stmt)
            success += 1
        except Exception as e:
            # 跳过已存在的库/表等常见错误
            err_msg = str(e).lower()
            if "already exists" in err_msg or "duplicate" in err_msg:
                continue
            print(f"  WARN: {e}")
            failed += 1
    cursor.close()
    conn.close()
    print(f"PASS: {filepath} 执行完成 ({success} 成功, {failed} 失败)")
    return failed == 0

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("用法: python run_sql_file.py <sql_file>")
        sys.exit(1)
    sys.exit(0 if run_sql_file(sys.argv[1]) else 1)
