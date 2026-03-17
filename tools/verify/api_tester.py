"""通用接口测试器 | python tools/verify/api_tester.py <test.json>"""
import urllib.request, urllib.error, json, sys

class ApiTester:
    def __init__(self, base="http://localhost:8080/api"):
        self.base, self.token, self.ok, self.fail, self.errors = base, None, 0, 0, []

    def login(self, ep, body):
        try:
            data = self._req("POST", ep, body)
            self.token = data.get("accessToken")
            if self.token: self.ok += 1; print("  PASS: 登录成功"); return True
        except Exception as e: pass
        self.fail += 1; print("  FAIL: 登录失败"); return False

    def test(self, name, method, path, body=None, expect_code=200, expect_http=200):
        print(f"  测试: {name}")
        try:
            url = f"{self.base}{path}"
            req = urllib.request.Request(url, json.dumps(body).encode() if body else None, method=method)
            req.add_header("Content-Type", "application/json")
            if self.token: req.add_header("Authorization", f"Bearer {self.token}")
            try:
                with urllib.request.urlopen(req, timeout=10) as r: http, rb = r.status, json.loads(r.read())
            except urllib.error.HTTPError as e:
                http = e.code
                try: rb = json.loads(e.read())
                except: rb = {"code": http}
            if http != expect_http:
                self.fail += 1; self.errors.append(f"{name}: HTTP期望{expect_http}实际{http}"); print(f"    FAIL"); return
            code = rb.get("code", http)
            if code != expect_code:
                self.fail += 1; self.errors.append(f"{name}: code期望{expect_code}实际{code}"); print(f"    FAIL"); return
            self.ok += 1; print(f"    PASS")
        except Exception as e:
            self.fail += 1; self.errors.append(f"{name}: {e}"); print(f"    FAIL: {e}")

    def summary(self):
        total = self.ok + self.fail
        print(f"\n{'='*50}\n测试: {self.ok}/{total} 通过, {self.fail} 失败")
        for e in self.errors: print(f"  - {e}")
        print("✅ ALL PASS" if not self.fail else "❌ HAS FAILURES")
        return self.fail == 0

    def _req(self, method, path, body=None):
        req = urllib.request.Request(f"{self.base}{path}", json.dumps(body).encode() if body else None, method=method)
        req.add_header("Content-Type", "application/json")
        if self.token: req.add_header("Authorization", f"Bearer {self.token}")
        with urllib.request.urlopen(req, timeout=10) as r: return json.loads(r.read()).get("data", {})

def run(fp):
    with open(fp, "r", encoding="utf-8") as f: cfg = json.load(f)
    t = ApiTester(cfg.get("baseUrl", "http://localhost:8080/api"))
    print(f"=== {cfg['module']} ===\n")
    lc = cfg.get("login")
    if lc:
        print(f"[登录] {lc['endpoint']}")
        if not t.login(lc["endpoint"], lc["body"]): return False
    for i, tc in enumerate(cfg.get("tests", []), 1):
        print(f"\n[{i}/{len(cfg['tests'])}] {tc['name']}")
        t.test(tc["name"], tc["method"], tc["path"], tc.get("body"), tc.get("expectCode", 200), tc.get("expectHttp", 200))
    return t.summary()

if __name__ == "__main__":
    if len(sys.argv) < 2: print("用法: python api_tester.py <test.json>"); sys.exit(1)
    sys.exit(0 if run(sys.argv[1]) else 1)
