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

Each service supports Spring profiles. **Pick the right one for your environment:**

| Profile | When to use | What it does |
|---|---|---|
| _(none / default)_ | Local dev on host, services connect to `localhost` | All hosts default to `localhost` |
| `docker-compose` | Running via `docker compose up` | All hosts use Docker service names (`mysql`, `redis`, etc.) |
| `local` | _(currently a synonym for default; reserved)_ | Same as default |
| `h2` | Unit tests with in-memory DB | Disables Redis, RabbitMQ, uses H2 |

**How to set:**

```bash
# Gradle
./gradlew :post-service:bootRun --args='--spring.profiles.active=docker-compose'

# Or via env
SPRING_PROFILES_ACTIVE=docker-compose ./gradlew :post-service:bootRun
```

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
