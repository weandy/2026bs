import pymysql
import json

def get_desc():
    try:
        conn = pymysql.connect(host='192.168.8.212', port=3306, user='root', password='mysql_eAk4W2', db='chp_resident')
        cursor = conn.cursor()
        cursor.execute("DESCRIBE visit_record;")
        for row in cursor.fetchall():
            print(row)
        conn.close()
    except Exception as e:
        print("Error:", e)

if __name__ == '__main__':
    get_desc()
