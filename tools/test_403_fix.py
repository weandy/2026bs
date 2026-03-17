import json, urllib.request

BASE = 'http://localhost:8080/api'

def get(url, token=None):
    req = urllib.request.Request(BASE + url)
    if token:
        req.add_header('Authorization', 'Bearer ' + token)
    try:
        r = urllib.request.urlopen(req)
        d = json.loads(r.read())
        return d.get('code'), d.get('data')
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()[:100]

def post(url, body):
    req = urllib.request.Request(BASE + url, data=json.dumps(body).encode(), headers={'Content-Type': 'application/json'})
    try:
        r = urllib.request.urlopen(req)
        d = json.loads(r.read())
        return d.get('code'), d.get('data')
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()[:100]

# 1. 居民登录
code, data = post('/auth/resident/login', {'phone': '13800000001', 'password': '123456'})
res_token = data['accessToken'] if code == 200 else None
print(f'居民登录: {code} {"✓" if code==200 else "✗"}')

# 2. 无 token 访问科室列表（应 200）
code, data = get('/admin/dept/list')
print(f'无token访问/admin/dept/list: {code} {"✓" if code==200 else "✗"}')

# 3. 居民 token 访问科室列表（应 200）
if res_token:
    code, data = get('/admin/dept/list', res_token)
    print(f'居民token访问/admin/dept/list: {code} {"✓" if code==200 else "✗"}')

# 4. 居民访问预约列表（应 200）
if res_token:
    code, data = get('/resident/appointment?page=1&size=5', res_token)
    print(f'居民预约列表: {code} {"✓" if code==200 else "✗"}')
