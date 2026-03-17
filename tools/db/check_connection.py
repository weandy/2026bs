"""
MySQL 连接检测脚本
用法：python tools/db/check_connection.py
"""
import json, os, sys

CONFIG_PATH = os.path.join(os.path.dirname(__file__), "db_config.json")
DEFAULT_CONFIG = {"host": "192.168.8.212", "port": 3306, "user": "root", "password": "mysql_eAk4W2"}

def load_config():
    if os.path.exists(CONFIG_PATH):
        with open(CONFIG_PATH, "r") as f: return json.load(f)
    with open(CONFIG_PATH, "w") as f: json.dump(DEFAULT_CONFIG, f, indent=2)
    return DEFAULT_CONFIG

def check():
    cfg = load_config()
    try:
        import pymysql
        conn = pymysql.connect(host=cfg["host"], port=cfg["port"], user=cfg["user"], password=cfg["password"], connect_timeout=5)
        conn.close()
        print(f"PASS: MySQL 连接成功 ({cfg['host']}:{cfg['port']})")
        return True
    except ImportError:
        try:
            import mysql.connector
            conn = mysql.connector.connect(host=cfg["host"], port=cfg["port"], user=cfg["user"], password=cfg["password"], connect_timeout=5)
            conn.close()
            print(f"PASS: MySQL 连接成功 ({cfg['host']}:{cfg['port']})")
            return True
        except ImportError:
            print("FAIL: 需要安装 pymysql: pip install pymysql")
            return False
        except Exception as e:
            print(f"FAIL: MySQL 连接失败 - {e}")
            return False
    except Exception as e:
        print(f"FAIL: MySQL 连接失败 - {e}")
        return False

if __name__ == "__main__":
    sys.exit(0 if check() else 1)
