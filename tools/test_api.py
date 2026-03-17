import json, urllib.request

base = 'http://localhost:8080/api'

# 1. 管理员登录 (AD001)
data = json.dumps({'username': 'AD001', 'password': '123456'}).encode()
req = urllib.request.Request(base + '/auth/admin/login', data=data, headers={'Content-Type': 'application/json'})
token = None
try:
    resp = urllib.request.urlopen(req)
    result = json.loads(resp.read())
    print('=== 管理员登录 (AD001) ===')
    print('code:', result.get('code'))
    print('name:', result.get('data', {}).get('name'))
    print('role:', result.get('data', {}).get('role'))
    token = result.get('data', {}).get('accessToken', '')
    print('token[:50]:', token[:50] + '...')
except Exception as e:
    print('管理员登录失败:', e)
    try:
        err = e.read().decode()
        print(err[:300])
    except: pass

# 2. 医生登录 (DR001)
data1b = json.dumps({'username': 'DR001', 'password': '123456'}).encode()
req1b = urllib.request.Request(base + '/auth/admin/login', data=data1b, headers={'Content-Type': 'application/json'})
try:
    resp1b = urllib.request.urlopen(req1b)
    result1b = json.loads(resp1b.read())
    print('\n=== 医生登录 (DR001) ===')
    print('code:', result1b.get('code'))
    print('name:', result1b.get('data', {}).get('name'))
    print('role:', result1b.get('data', {}).get('role'))
    dr_token = result1b.get('data', {}).get('accessToken', '')
    print('token[:50]:', dr_token[:50] + '...')
except Exception as e:
    print('医生登录失败:', e)
    try:
        print(e.read().decode()[:300])
    except: pass

# 3. 居民登录 (13800000001)
data2 = json.dumps({'phone': '13800000001', 'password': '123456'}).encode()
req2 = urllib.request.Request(base + '/auth/resident/login', data=data2, headers={'Content-Type': 'application/json'})
try:
    resp2 = urllib.request.urlopen(req2)
    result2 = json.loads(resp2.read())
    print('\n=== 居民登录 (13800000001) ===')
    print('code:', result2.get('code'))
    print('name:', result2.get('data', {}).get('name'))
    res_token = result2.get('data', {}).get('accessToken', '')
    print('token[:50]:', res_token[:50] + '...')
except Exception as e:
    print('居民登录失败:', e)
    try:
        print(e.read().decode()[:300])
    except: pass

# 4. 员工列表（管理员身份）
if token:
    req3 = urllib.request.Request(base + '/admin/staff?page=1&size=5')
    req3.add_header('Authorization', 'Bearer ' + token)
    try:
        resp3 = urllib.request.urlopen(req3)
        result3 = json.loads(resp3.read())
        print('\n=== 员工列表 (需认证) ===')
        print('code:', result3.get('code'))
        records = result3.get('data', {}).get('records', [])
        print('count:', len(records))
        for s in records[:3]:
            print(f"  - {s.get('username')} / {s.get('name')} / {s.get('deptName')}")
    except Exception as e:
        print('员工列表失败:', e)
        try: print(e.read().decode()[:300])
        except: pass

# 5. 公告列表
if token:
    req4 = urllib.request.Request(base + '/admin/notice?page=1&size=5')
    req4.add_header('Authorization', 'Bearer ' + token)
    try:
        resp4 = urllib.request.urlopen(req4)
        result4 = json.loads(resp4.read())
        print('\n=== 公告列表 ===')
        print('code:', result4.get('code'))
        notices = result4.get('data', {}).get('records', [])
        print('count:', len(notices))
        for n in notices[:2]:
            print(f"  - {n.get('title')}")
    except Exception as e:
        print('公告列表失败:', e)
        try: print(e.read().decode()[:300])
        except: pass
