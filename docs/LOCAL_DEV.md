# Local Development

How to run the platform without Docker for fast iteration. For the full Docker setup, see the project `README.md`.

---

## Prerequisites

- **Java 17** (Eclipse Temurin recommended)
- **Node.js 22.11+** (for the frontend)
- **Docker** (recommended for MySQL / Redis / RabbitMQ / MinIO)
- **Gradle 8.x** (the wrapper `./gradlew` is included; you don't need a system install)

> On Windows, use `gradlew.bat` from `cmd.exe` or `./gradlew` from Git Bash / WSL.

---

## Profiles

Each service supports Spring profiles. **Pick the right one for your
environment.** The three "modes" most people will use correspond to the
profiles below; the profile you activate must match the mode you are
running in or the services will fail to reach MySQL / Redis / RabbitMQ.

| Mode         | Active profile | When to use it | What it does |
|--------------|----------------|----------------|--------------|
| **Dev** (hybrid) | _(none / `local`)_ | Stateful services in Docker, gateway + the service you're editing on the host (`./gradlew :service:bootRun`) | All hosts default to `localhost`; monolith listens on `8080`, gateway on `8080` — see [Troubleshooting § Port 8080 conflict](#port-8080-conflict-monolith-vs-gateway) |
| **Hybrid**  | _(none / `local`)_ | Same as Dev. Use this mode when the service you are editing needs to talk to another service on the host instead of in Docker. | Identical wiring to Dev. |
| **Docker**  | `docker-compose` | `docker compose up` for the full 11-container stack | All hosts use Docker service names (`mysql`, `redis`, `rabbitmq`, `media-service`, `profile-service`, `post-service`, `notification-service`, `feed-service`) |
| **Test**    | `h2` | Unit tests only | In-memory H2; Redis listeners disabled; RabbitMQ auto-startup off |

> **Note on `dev` vs `hybrid`.** In the current codebase there is no
> separate `dev` profile — both modes activate the default /
> `local` profile and rely on `localhost` for cross-service hosts. The
> difference is operational: in **Dev** you are running one service
> locally against the Docker sidecars; in **Hybrid** you may be running
> several services on the host against the same sidecars. The rules
> below apply to both.

**How to set the profile:**

```bash
# Gradle (passes --spring.profiles.active to the JVM)
./gradlew :post-service:bootRun --args='--spring.profiles.active=docker-compose'

# Or via env
SPRING_PROFILES_ACTIVE=docker-compose ./gradlew :post-service:bootRun

# Or in docker-compose.yml (already set on every Java service)
environment:
  - SPRING_PROFILES_ACTIVE=docker-compose
```

**How to choose between Dev and Docker:**

- Use **Dev / Hybrid** when you are iterating on a single Java service
  (or the frontend) and want fast rebuild + restart cycles.
- Use **Docker** when you want to validate the full system end-to-end,
  test OAuth, or demo the platform.

---

## Option A: Full Docker (recommended)

```bash
# 1. Build all jars
./gradlew build -x test

# 2. Build and start all services
docker compose up -d --build

# 3. Tail logs
docker compose logs -f

# 4. Open the app
# Frontend: http://localhost:3000
# Gateway:  http://localhost:8080
# Swagger:  http://localhost:8080/swagger-ui/index.html
```

Stop with `docker compose down` (preserves volumes) or `docker compose down -v` (wipes everything).

---

## Option B: Hybrid (Docker for stateful services, host for Java)

Useful for fast iteration on a single service while keeping MySQL/Redis/RabbitMQ in Docker.

```bash
# 1. Start only the stateful services
docker compose up -d mysql redis rabbitmq minio

# 2. Run the gateway and the service you're iterating on, in their own terminals
./gradlew :api-gateway:bootRun
./gradlew :post-service:bootRun
./gradlew :frontend:npmRunDev   # not a real task — use the npm script

# 3. In the frontend directory
cd frontend && npm run dev
```

The Vite dev server proxies `/api` to `http://localhost:8080` (the gateway). See `frontend/vite.config.ts`.

---

## Option C: Pure local (no Docker)

You need MySQL, Redis, RabbitMQ (with STOMP plugin), and MinIO installed natively.

```bash
# 1. Create databases
mysql -uroot -p < mysql-init.sql

# 2. Start MinIO
minio server /tmp/minio-data --console-address ":9001"

# 3. Start RabbitMQ with STOMP enabled
rabbitmq-plugins enable rabbitmq_stomp
rabbitmq-server

# 4. Start each Java service
./gradlew :social-network:bootRun
./gradlew :profile-service:bootRun
./gradlew :post-service:bootRun
./gradlew :feed-service:bootRun
./gradlew :notification-service:bootRun
./gradlew :api-gateway:bootRun

# 5. Start the frontend
cd frontend && npm run dev
```

> The monolith's port is `8080`. The gateway is also `8080`. They will conflict. Either run the monolith and stop the gateway, or change one of the ports.

---

## Environment variables

| Var | Default | Used by | Notes |
|---|---|---|---|
| `MYSQL_ROOT_PASSWORD` | `app` | MySQL container | |
| `MYSQL_USER` | `app` | MySQL container | |
| `MYSQL_PASSWORD` | `app` | MySQL container | |
| `REDIS_PASSWORD` | `devpassword` | Redis + all services | |
| `MINIO_ROOT_USER` | `minioadmin` | MinIO container | |
| `MINIO_ROOT_PASSWORD` | `minioadmin` | MinIO container | |
| `RABBITMQ_DEFAULT_USER` | `guest` | RabbitMQ container | |
| `RABBITMQ_DEFAULT_PASS` | `guest` | RabbitMQ container | |
| `GOOGLE_CLIENT_ID` | _(required for OAuth2)_ | Monolith | Get from Google Cloud Console |
| `GOOGLE_CLIENT_SECRET` | _(required for OAuth2)_ | Monolith | Get from Google Cloud Console |
| `VITE_API_BASE_URL` | `/api` | Frontend | Vite env var |
| `VITE_MEDIA_BASE_URL` | `http://localhost:9000/spring-boot` | Frontend | Vite env var |

For Docker, set these in `.env` (or `.env.local`). For local dev, export them in your shell.

---

## Common tasks

| Task | Command |
|---|---|
| Build everything | `./gradlew build -x test` |
| Build a single service | `./gradlew :post-service:build -x test` |
| Run a single service | `./gradlew :post-service:bootRun` |
| Run tests | `./gradlew test` |
| Run the frontend in dev | `cd frontend && npm run dev` |
| Build the frontend | `cd frontend && npm run build` |
| Reset all data | `docker compose down -v` |
| View queue topology | `docker exec -it <rabbitmq> rabbitmqctl list_queues` |
| View service logs | `docker compose logs -f post-service` |
| Open a shell in a service container | `docker compose exec post-service sh` |
| Seed the database (after Phase 5) | `bash seed.sh` or `powershell seed.ps1` |

---

## Troubleshooting

### `npm install` fails with `EACCES` (Linux/Mac)
Use `sudo chown -R $(whoami) ~/.npm` or run with `npm install --no-optional`.

### `npm install` succeeds but `lucide-react` icons are missing
The repository pins `lucide-react@^1.17.0` (legacy v1 line). See `docs/DECISIONS.md` ADR — this is being fixed in Phase 0.

### `./gradlew build` fails with `JAVA_HOME` not set
Set `JAVA_HOME` to your Java 17 install:
```bash
export JAVA_HOME=/path/to/jdk-17   # Mac/Linux
set JAVA_HOME=C:\path\to\jdk-17    # Windows cmd
$env:JAVA_HOME = "C:\path\to\jdk-17"  # PowerShell
```

### `docker compose up` says `port 3306 is already allocated`
Another MySQL is running on the host. Stop it, or change the port mapping in `docker-compose.yml`.

### Services start but the frontend shows "Failed to fetch"
- Check the cookie: open DevTools → Application → Cookies. `SESSION` should exist, not `Secure`.
- Check CORS: open DevTools → Network → look for failed preflight (OPTIONS) requests.
- Check the gateway: `curl http://localhost:8080/actuator/health` should return `{"status":"UP"}`.

### The app loads but every API returns 401
The session cookie is not being set or sent. Most common cause: `Secure` cookie over plain HTTP. See `docs/DECISIONS.md` ADR-001 (this is fixed in Phase 1).

### "Connection refused" on gRPC calls
A microservice tried to call another before it was ready. Wait 30s after `docker compose up`. In `docker-compose.yml`, the `depends_on` should be `condition: service_healthy` (this is fixed in Phase 1).

### Port 8080 conflict (monolith vs gateway)
**Symptom:** Either the gateway fails to start with `BindException: Address already in use` (when running the gateway on the host) **or** the monolith fails to start in Docker with the same error.
**Cause:** Both services default to HTTP port `8080`. In Docker the host port for the monolith is mapped to `8081` (`127.0.0.1:8081:8080`) so the gateway can own host `8080`. If you start the monolith on the host with `./gradlew :social-network:bootRun` it will collide with a host-side gateway.
**Fix:**
- For **Docker**: do not start the monolith on the host. The Docker container uses `8080` internally and the host port is `8081`.
- For **Dev / Hybrid**: stop the gateway on the host and run the monolith on `8080` directly, **or** start the gateway and reach the monolith only through it (the monolith container is on `8081` in that case).
- Override the port on either side with `SERVER_PORT=9099 ./gradlew :service:bootRun` if you need both up at once.

### Redis session not shared (wrong namespace)
**Symptom:** Login works on the monolith (you get a `SESSION` cookie and `/auth/inspect` returns the user) but every call to a microservice returns `401`. Or: the SPA shows the user logged in, but `/api/profiles/me` is empty.
**Cause:** A consuming service is reading the session from a different Redis key prefix than the monolith is writing. The canonical namespace is `engineerpro:app` (set by `SessionConfig` in `socialnetwork-common`).
**Fix:**
- Make sure every service `implementation project(':socialnetwork-common')` (see [`socialnetwork-common/README.md`](../socialnetwork-common/README.md)).
- Make sure every service's `SecurityFilterChain` (or implicit config) `@Import`s `SessionConfig` and that the `@EnableRedisHttpSession(redisNamespace = "engineerpro:app")` annotation is present.
- Inspect Redis directly: `docker exec -it redis redis-cli -a devpassword KEYS '*engineerpro:app*'` — you should see at least one session key after a successful login.

### OAuth redirect URI mismatch
**Symptom:** Google login redirects back to the app but the user is not authenticated; or Google shows `Error: redirect_uri_mismatch`.
**Cause:** The OAuth client in [Google Cloud Console](https://console.cloud.google.com/apis/credentials) does not list the URI Spring Security is sending.
**Fix:**
- Add the following **Authorized redirect URIs** to your OAuth 2.0 Client ID:
  - `http://localhost:8080/login/oauth2/code/google` (when going through the gateway)
  - `http://localhost:8081/login/oauth2/code/google` (when going through the monolith directly in Docker)
  - `http://localhost:3000/login/oauth2/code/google` (when going through the SPA proxy in dev)
- The path `/login/oauth2/code/{registrationId}` is the Spring Security default; if you customized `CommonOAuth2Provider` you will need a different path.
- Restart the monolith after changing `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` in `.env` — they are read at startup.

### gRPC connection refused (wrong host/port)
**Symptom:** A service log shows `UNAVAILABLE: io exception` or `Status{code=UNAVAILABLE, description=Connection refused` when calling another service. Often seen on first boot of `post-service`, `feed-service`, or `notification-service`.
**Cause:** The caller is dialing the wrong host or port. In **Dev** mode the default is `localhost:<gRPC-port>`; in **Docker** mode the default is the Docker service name (e.g. `profile-service:9092`).
**Fix:**
- Confirm the env vars are set (or the defaults are correct for your mode):
  - `PROFILE_SERVICE_HOST` (default `localhost` / `profile-service` in Docker)
  - `PROFILE_SERVICE_GRPC_PORT` (default `9092`)
  - `MEDIA_SERVICE_HOST` / `MEDIA_SERVICE_GRPC_PORT` (`localhost:9091` / `media-service:9091`)
  - `POST_SERVICE_HOST` / `POST_SERVICE_GRPC_PORT` (`localhost:9093` / `post-service:9093`)
- Wait for `condition: service_started` (or `service_healthy`) to be satisfied — `docker compose ps` should show the target service as `running`/`healthy` before the caller attempts to dial.
- For local (non-Docker) runs, the gRPC servers must be running on `localhost` for the expected ports. `ss -lntp | grep 909` (Linux/Mac) or `Get-NetTCPConnection -LocalPort 909*` (PowerShell) is the fastest check.

### MinIO bucket not found (run init script)
**Symptom:** Uploading a post fails with `Bucket 'spring-boot' does not exist` or `NoSuchBucket`, and the post is rejected (`InvalidInputException` after Phase 4 fail-fast is in).
**Cause:** The MinIO container starts with an empty data volume and the bucket has not been created.
**Fix:**
- Create the bucket manually via the MinIO console:
  1. Open <http://localhost:9001> (`minioadmin` / `minioadmin`).
  2. **Buckets → Create Bucket →** name `spring-boot` → **Create**.
- Or via the `mc` CLI:
  ```bash
  docker exec -it minio mc alias set local http://localhost:9000 minioadmin minioadmin
  docker exec -it minio mc mb local/spring-boot
  ```
- For a clean dev experience, add a one-shot init step to `docker-compose.yml` or a `minio-init` sidecar that runs on first boot. The bucket survives across `docker compose down` / `up` cycles as long as you don't pass `-v`.
