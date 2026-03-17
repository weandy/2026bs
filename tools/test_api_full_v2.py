"""
社区健康平台 - 全量覆盖测试 v2
覆盖: 认证 + 管理 + 预约 + 药品 + 处方 + 公告 + 随访 + 报表 + 调班 + 个人中心 + PDF + WebSocket
"""
import json, urllib.request, socket, sys

base = 'http://localhost:8080/api'
passed = 0
failed = 0
errors = []

def test(name, method, url, data=None, token=None, expect_code=200):
    global passed, failed, errors
    try:
        if data is not None:
            req = urllib.request.Request(url, data=json.dumps(data).encode(),
                                        headers={'Content-Type': 'application/json'})
        else:
            req = urllib.request.Request(url)
        if method == 'PUT':
            req.get_method = lambda: 'PUT'
        elif method == 'DELETE':
            req.get_method = lambda: 'DELETE'
        elif method == 'POST' and data is None:
            req = urllib.request.Request(url, data=b'', method='POST')
        if token:
            req.add_header('Authorization', 'Bearer ' + token)
        resp = urllib.request.urlopen(req)
        body = json.loads(resp.read()) if resp.headers.get('Content-Type', '').startswith('application/json') else {}
        code = body.get('code', resp.status)
        if code == expect_code or resp.status == expect_code:
            passed += 1
            print(f'  PASS {name}')
        else:
            failed += 1
            errors.append(f'{name}: expected {expect_code}, got {code}')
            print(f'  FAIL {name} (code={code})')
    except urllib.error.HTTPError as e:
        if e.code == expect_code:
            passed += 1
            print(f'  PASS {name} (expected {expect_code})')
        else:
            failed += 1
            errors.append(f'{name}: HTTP {e.code}')
            print(f'  FAIL {name} (HTTP {e.code})')
    except Exception as e:
        failed += 1
        errors.append(f'{name}: {str(e)[:60]}')
        print(f'  FAIL {name} ({e})')

def test_binary(name, method, url, token=None, expect_type='application/pdf'):
    global passed, failed, errors
    try:
        req = urllib.request.Request(url, data=b'', method=method)
        if token:
            req.add_header('Authorization', 'Bearer ' + token)
        resp = urllib.request.urlopen(req)
        ct = resp.headers.get('Content-Type', '')
        body = resp.read()
        if expect_type in ct and len(body) > 0:
            passed += 1
            print(f'  PASS {name} ({len(body)} bytes)')
        else:
            failed += 1
            errors.append(f'{name}: type={ct}, len={len(body)}')
            print(f'  FAIL {name}')
    except Exception as e:
        failed += 1
        errors.append(f'{name}: {str(e)[:60]}')
        print(f'  FAIL {name} ({e})')

# ===== AUTH =====
print('--- AUTH ---')
test('admin_login', 'POST', base+'/auth/admin/login', {'username':'AD001','password':'123456'})
# get admin token
r = json.loads(urllib.request.urlopen(urllib.request.Request(base+'/auth/admin/login',
    data=json.dumps({'username':'AD001','password':'123456'}).encode(),
    headers={'Content-Type':'application/json'})).read())
admin_token = r['data']['accessToken']

test('doctor_login', 'POST', base+'/auth/admin/login', {'username':'DR001','password':'123456'})
r2 = json.loads(urllib.request.urlopen(urllib.request.Request(base+'/auth/admin/login',
    data=json.dumps({'username':'DR001','password':'123456'}).encode(),
    headers={'Content-Type':'application/json'})).read())
dr_token = r2['data']['accessToken']

test('resident_login', 'POST', base+'/auth/resident/login', {'phone':'13800000002','password':'123456'})
r3 = json.loads(urllib.request.urlopen(urllib.request.Request(base+'/auth/resident/login',
    data=json.dumps({'phone':'13800000002','password':'123456'}).encode(),
    headers={'Content-Type':'application/json'})).read())
res_token = r3['data']['accessToken']

test('wrong_password', 'POST', base+'/auth/admin/login', {'username':'AD001','password':'wrong'}, expect_code=401)

# ===== ADMIN =====
print('--- ADMIN ---')
test('staff_list', 'GET', base+'/admin/staff?page=1&size=5', token=admin_token)
test('notice_list', 'GET', base+'/admin/notice?page=1&size=5', token=admin_token)
test('dept_list', 'GET', base+'/admin/dept/list', token=admin_token)
test('drug_list', 'GET', base+'/admin/drug?page=1&size=5', token=admin_token)
test('schedule_list', 'GET', base+'/admin/schedule?page=1&size=5', token=admin_token)
test('audit_log', 'GET', base+'/admin/audit-log?page=1&size=5', token=admin_token)
test('sys_config', 'GET', base+'/admin/config', token=admin_token)

# ===== REPORT =====
print('--- REPORT ---')
test('report_overview', 'GET', base+'/admin/report/overview', token=admin_token)
test('report_drug', 'GET', base+'/admin/report/drug?startDate=2026-01-01&endDate=2026-03-15', token=admin_token)

# ===== SCHEDULE TRANSFER =====
print('--- SCHEDULE TRANSFER ---')
test('transfer_list', 'GET', base+'/admin/schedule/transfer?page=1&size=10', token=admin_token)
test('transfer_submit', 'POST', base+'/admin/schedule/transfer', {'scheduleId':1,'reason':'test'}, token=admin_token)

# ===== RESIDENT =====
print('--- RESIDENT ---')
test('appointment_schedules', 'GET', base+'/resident/appointment/schedules?deptCode=QKMZ&date=2026-03-20', token=res_token)
test('visit_records', 'GET', base+'/resident/visit-record?page=1&size=5', token=res_token)
test('health_record', 'GET', base+'/resident/health-record', token=res_token)
test('vaccine_record', 'GET', base+'/resident/vaccine/records', token=res_token)

# ===== RESIDENT PROFILE =====
print('--- RESIDENT PROFILE ---')
test('get_profile', 'GET', base+'/resident/profile', token=res_token)

# ===== MEDICAL =====
print('--- MEDICAL ---')
test('workbench_queue', 'GET', base+'/medical/workbench/queue?deptCode=QKMZ', token=dr_token)
test('prescription_visit', 'GET', base+'/medical/prescription/visit/1', token=dr_token)

# ===== FOLLOW-UP =====
print('--- FOLLOW-UP ---')
test('followup_plan', 'GET', base+'/medical/follow-up/plan?page=1&size=5', token=dr_token)
test('followup_today', 'GET', base+'/medical/follow-up/plan/today-due', token=dr_token)
test('followup_trend', 'GET', base+'/medical/follow-up/trend/1?limit=10', token=dr_token)
test('public_health', 'GET', base+'/medical/public-health?page=1&size=5', token=dr_token)

# ===== PDF =====
print('--- PDF ---')
test_binary('prescription_pdf', 'POST', base+'/medical/prescription/1/pdf', token=dr_token)

# ===== PERMISSION BOUNDARY =====
print('--- PERMISSION ---')
# 居民 Token 访问 admin 接口应 403
test('resident_access_admin', 'GET', base+'/admin/staff?page=1&size=5', token=res_token, expect_code=403)
# 居民 Token 访问 medical 接口应 403
test('resident_access_medical', 'GET', base+'/medical/workbench/queue', token=res_token, expect_code=403)
# 医生 Token 访问 admin 接口应 403
test('doctor_access_admin', 'GET', base+'/admin/config', token=dr_token, expect_code=403)
# 管理员 Token 访问 resident 接口应 403
test('admin_access_resident', 'GET', base+'/resident/profile', token=admin_token, expect_code=403)

# ===== TOKEN REFRESH =====
print('--- TOKEN REFRESH ---')
test('token_refresh', 'POST', base+'/auth/refresh', {'refreshToken': r3['data']['refreshToken']})

# ===== PUBLIC =====
print('--- PUBLIC ---')
test('public_screen', 'GET', base+'/public/screen/QKMZ')

# ===== EXCEPTION / EDGE CASES =====
print('--- EXCEPTION ---')
# 无 Token 访问受保护接口 → 401
test('no_token_401', 'GET', base+'/admin/staff?page=1&size=5', expect_code=401)
# 错误密码登录 → 业务错误码
test('wrong_password', 'POST', base+'/auth/admin/login', {'username':'AD001','password':'wrong_pw'}, expect_code=401)
# 不存在的居民登录 → 业务错误码
test('nonexist_resident', 'POST', base+'/auth/resident/login', {'phone':'00000000000','password':'123456'}, expect_code=401)
# 不存在的排班 ID 停止 → 应返回异常
test('stop_nonexist_schedule', 'PUT', base+'/admin/schedule/99999/stop', {'reason':'test'}, token=admin_token, expect_code=404)
# 管理员创建公告（有效）
test('create_notice', 'POST', base+'/admin/notice', {'title':'测试公告','content':'异常测试中创建的公告'}, token=admin_token)
# 删除不存在的公告（应成功但无影响，MP deleteById 不报错）
test('delete_nonexist_notice', 'DELETE', base+'/admin/notice/99999', token=admin_token)
# 居民端无 Token 访问 profile → 401
test('resident_no_token_401', 'GET', base+'/resident/profile', expect_code=401)
# 医生端访问居民接口 → 403
test('doctor_access_resident', 'GET', base+'/resident/profile', token=dr_token, expect_code=403)

# ===== WEBSOCKET =====
print('--- WEBSOCKET ---')
try:
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.settimeout(5)
    s.connect(('127.0.0.1', 8080))
    s.send(b'GET /api/ws/queue/QKMZ HTTP/1.1\r\nHost: 127.0.0.1\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\r\nSec-WebSocket-Version: 13\r\n\r\n')
    resp = s.recv(1024).decode()
    s.close()
    if '101' in resp:
        passed += 1
        print('  PASS ws_handshake (101)')
    else:
        failed += 1
        errors.append('ws_handshake: ' + resp[:50])
        print('  FAIL ws_handshake')
except Exception as e:
    failed += 1
    errors.append(f'ws_handshake: {e}')
    print(f'  FAIL ws_handshake ({e})')

# ===== SUMMARY =====
print()
print(f'=== RESULT: {passed} passed, {failed} failed ===')
if errors:
    for e in errors:
        print(f'  ERROR: {e}')
sys.exit(1 if failed > 0 else 0)
