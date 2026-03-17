"""
SQL 查询脚本
用法：python tools/db/query.py "SELECT ..."
"""
import json, os, sys

CONFIG_PATH = os.path.join(os.path.dirname(__file__), "db_config.json")

def load_config():
    with open(CONFIG_PATH, "r") as f: return json.load(f)

def query(sql):
    cfg = load_config()
    try:
        import pymysql
        conn = pymysql.connect(host=cfg["host"], port=cfg["port"], user=cfg["user"], password=cfg["password"],
                               charset="utf8mb4")
    except ImportError:
        import mysql.connector
        conn = mysql.connector.connect(host=cfg["host"], port=cfg["port"], user=cfg["user"], password=cfg["password"],
                                       charset="utf8mb4")
    cursor = conn.cursor()
    cursor.execute(sql)
    rows = cursor.fetchall()
    cols = [desc[0] for desc in cursor.description] if cursor.description else []
    cursor.close()
    conn.close()
    if cols: print(" | ".join(cols))
    if cols: print("-" * (len(" | ".join(cols))))
    for row in rows:
        print(" | ".join(str(v) for v in row))
    if not rows: print("(空结果)")
    return rows

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print('用法: python query.py "SELECT ..."')
        sys.exit(1)
    query(sys.argv[1])
