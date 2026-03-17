import json, urllib.request, sys

base = 'http://localhost:8080/api'
passed = 0
failed = 0
errors = []

def test(name, method, url, data=None, token=None, expect_code=200):
    global passed, failed, errors
    try:
        if data is not None:
            req = urllib.request.Request(base + url, data=json.dumps(data).encode(), headers={'Content-Type': 'application/json'})
        else:
            req = urllib.request.Request(base + url)
        if method in ('PUT', 'DELETE'):
            req.get_method = lambda: method
        if token:
            req.add_header('Authorization', 'Bearer ' + token)
        resp = urllib.request.urlopen(req)
        result = json.loads(resp.read())
        code = result.get('code', resp.status)
        if code == expect_code:
            passed += 1
            detail = ''
            d = result.get('data')
            if isinstance(d, dict) and 'records' in d: detail = f" ({len(d['records'])} records)"
            elif isinstance(d, list): detail = f" ({len(d)} items)"
            print(f'  PASS {name}{detail}')
        else:
            failed += 1; errors.append(f'{name}: expected {expect_code} got {code}'); print(f'  FAIL {name} (code={code})')
        return result
    except urllib.error.HTTPError as e:
        body = ''
        try: body = e.read().decode()[:200]
        except: pass
        if e.code == expect_code:
            passed += 1; print(f'  PASS {name} (expected {expect_code})'); return None
        failed += 1; errors.append(f'{name}: HTTP {e.code} {body[:100]}'); print(f'  FAIL {name} (HTTP {e.code}: {body[:80]})'); return None
    except Exception as e:
        failed += 1; errors.append(f'{name}: {str(e)[:100]}'); print(f'  FAIL {name} ({str(e)[:80]})'); return None

# Login
print('--- AUTH ---')
r1 = test('admin_login', 'POST', '/auth/admin/login', {'username': 'AD001', 'password': '123456'})
admin_token = r1['data']['accessToken'] if r1 and r1.get('data') else None
r2 = test('doctor_login', 'POST', '/auth/admin/login', {'username': 'DR001', 'password': '123456'})
dr_token = r2['data']['accessToken'] if r2 and r2.get('data') else None
r3 = test('resident_login', 'POST', '/auth/resident/login', {'phone': '13800000001', 'password': '123456'})
res_token = r3['data']['accessToken'] if r3 and r3.get('data') else None
test('wrong_password', 'POST', '/auth/admin/login', {'username': 'AD001', 'password': 'wrong'}, expect_code=401)

# Admin
print('\n--- ADMIN ---')
if admin_token:
    test('staff_list', 'GET', '/admin/staff?page=1&size=5', token=admin_token)
    test('sys_config', 'GET', '/admin/config', token=admin_token)
    test('notice_list', 'GET', '/admin/notice?page=1&size=5', token=admin_token)
    test('audit_log', 'GET', '/admin/audit-log?page=1&size=5', token=admin_token)
    test('schedule_list', 'GET', '/admin/schedule?page=1&size=5', token=admin_token)
    test('drug_list', 'GET', '/admin/drug?page=1&size=5', token=admin_token)

# Resident
print('\n--- RESIDENT ---')
if res_token:
    test('health_record', 'GET', '/resident/health-record', token=res_token)
    test('my_appointments', 'GET', '/resident/appointment?page=1&size=5', token=res_token)
    test('visit_records', 'GET', '/resident/visit-record?page=1&size=5', token=res_token)
    test('vaccine_appts', 'GET', '/resident/vaccine/appointments?page=1&size=5', token=res_token)
    test('vaccine_records', 'GET', '/resident/vaccine/records', token=res_token)
    test('messages', 'GET', '/resident/message?page=1&size=5', token=res_token)
    test('unread_count', 'GET', '/resident/message/unread-count', token=res_token)

# Medical
print('\n--- MEDICAL ---')
if dr_token:
    test('today_queue', 'GET', '/medical/workbench/queue', token=dr_token)
    test('followup_plans', 'GET', '/medical/follow-up/plan?page=1&size=5', token=dr_token)
    test('followup_today', 'GET', '/medical/follow-up/plan/today-due', token=dr_token)
    test('public_health', 'GET', '/medical/public-health?page=1&size=5', token=dr_token)

# Summary
print(f'\n=== RESULT: {passed} passed, {failed} failed ===')
if errors:
    print('Failures:')
    for e in errors: print(f'  - {e}')
sys.exit(0 if failed == 0 else 1)
