import pymysql

# 检查 staff 表
conn = pymysql.connect(host='192.168.8.212', port=3306, user='root', password='mysql_eAk4W2', database='chp_admin')
cur = conn.cursor()
cur.execute('SELECT id, username, name, password, status FROM staff LIMIT 10')
print('=== chp_admin.staff ===')
for r in cur.fetchall():
    print(f'  id={r[0]} username={r[1]} name={r[2]} password={r[3][:30]}... status={r[4]}')
conn.close()

# 检查 resident 表
conn2 = pymysql.connect(host='192.168.8.212', port=3306, user='root', password='mysql_eAk4W2', database='chp_resident')
cur2 = conn2.cursor()
cur2.execute('SELECT id, phone, name, password, status FROM resident LIMIT 10')
print('\n=== chp_resident.resident ===')
for r in cur2.fetchall():
    print(f'  id={r[0]} phone={r[1]} name={r[2]} password={r[3][:30]}... status={r[4]}')
conn2.close()
