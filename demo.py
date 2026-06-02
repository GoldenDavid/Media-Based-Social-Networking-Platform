import urllib.request
import json

def fetch(url, data=None, session_cookie=None):
    headers = {'Content-Type': 'application/json', 'Accept': 'application/json'}
    if session_cookie:
        headers['Cookie'] = session_cookie
    
    req_data = json.dumps(data).encode('utf-8') if data else None
    req = urllib.request.Request(url, data=req_data, headers=headers)
    
    try:
        with urllib.request.urlopen(req) as response:
            cookie = response.headers.get('Set-Cookie')
            body = response.read().decode('utf-8')
            return response.status, body, cookie
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode('utf-8'), None
    except Exception as e:
        return 0, str(e), None

print("=== STEP 1: REGISTER ===")
status, body, _ = fetch("http://localhost:8081/auth/register", {"name":"Demo User","username":"demouser_v5","password":"demo1234"})
print(f"Status {status}: {body}\n")

print("=== STEP 2: LOGIN ===")
status, body, cookie = fetch("http://localhost:8081/auth/login", {"username":"demouser_v5","password":"demo1234"})
print(f"Status {status}: {body}")
session_cookie = None
if cookie:
    session_cookie = cookie.split(';')[0]
print(f"Cookie: {session_cookie}\n")

print("=== STEP 3: INSPECT SESSION ===")
status, body, _ = fetch("http://localhost:8081/auth/inspect", session_cookie=session_cookie)
print(f"Status {status}: {body}\n")

print("=== STEP 4: FEED (via gateway) ===")
status, body, _ = fetch("http://localhost:8080/dynamic-feeds?page=1&limit=5", session_cookie=session_cookie)
if status == 200:
    data = json.loads(body)
    print(f"Status 200: totalPages={data.get('totalPage')} postCount={len(data.get('posts', []))}")
    for p in data.get('posts', [])[:3]:
        print(f"  Post[{p.get('id')}]: '{p.get('caption')}' by @{p.get('createdBy', {}).get('username')}")
else:
    print(f"Status {status}: {body}")

print("\n=== STEP 5: MY PROFILE (via gateway) ===")
status, body, _ = fetch("http://localhost:8080/profiles/me", session_cookie=session_cookie)
print(f"Status {status}: {body}\n")
