"""后端健康检查 | python tools/verify/check_health.py [--wait]"""
import urllib.request, json, sys, time
def check(wait=False):
    deadline = time.time() + (60 if wait else 5)
    while time.time() < deadline:
        try:
            with urllib.request.urlopen("http://localhost:8080/actuator/health", timeout=5) as r:
                if json.loads(r.read()).get("status") == "UP":
                    print("PASS: 后端服务正常"); return True
        except: pass
        if not wait: print("FAIL: 无法连接后端"); return False
        print(f"等待后端启动... ({int(deadline-time.time())}s)"); time.sleep(3)
    print("FAIL: 超时"); return False
if __name__ == "__main__": sys.exit(0 if check("--wait" in sys.argv) else 1)
