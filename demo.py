import urllib.request
import json

def fetch(url, data=None, token=None):
    headers = {'Content-Type': 'application/json', 'Accept': 'application/json'}
    if token:
        headers['Authorization'] = f'Bearer {token}'
    
    req_data = json.dumps(data).encode('utf-8') if data else None
    req = urllib.request.Request(url, data=req_data, headers=headers)
    
    try:
        with urllib.request.urlopen(req) as response:
            body = response.read().decode('utf-8')
            return response.status, body
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode('utf-8')
    except Exception as e:
        return 0, str(e)

print("=== STEP 1: REGISTER ===")
status, body = fetch("http://localhost:8081/auth/register", {"name":"Demo User","username":"demouser_v5","password":"demo1234"})
print(f"Status {status}: {body}\n")

print("=== STEP 2: LOGIN ===")
status, body = fetch("http://localhost:8081/auth/login", {"username":"demouser_v5","password":"demo1234"})
print(f"Status {status}: {body}")
token = None
if status == 200:
    data = json.loads(body)
    token = data.get('token')
print(f"Token: {token}\n")

print("=== STEP 3: INSPECT SESSION ===")
status, body = fetch("http://localhost:8081/auth/inspect", token=token)
print(f"Status {status}: {body}\n")

print("=== STEP 4: FEED (via gateway) ===")
status, body = fetch("http://localhost:8080/dynamic-feeds?page=1&limit=5", token=token)
if status == 200:
    data = json.loads(body)
    print(f"Status 200: totalPages={data.get('totalPage')} postCount={len(data.get('posts', []))}")
    for p in data.get('posts', [])[:3]:
        print(f"  Post[{p.get('id')}]: '{p.get('caption')}' by @{p.get('createdBy', {}).get('username')}")
else:
    print(f"Status {status}: {body}")

print("\n=== STEP 5: MY PROFILE (via gateway) ===")
status, body = fetch("http://localhost:8080/profiles/me", token=token)
print(f"Status {status}: {body}\n")
