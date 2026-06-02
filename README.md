# Media-Based Social Networking Platform

A media-first social network built on a **Java Spring Boot** backend and a
**React 19** frontend. The platform is organized as a **monolith + microservices**
hybrid: the original Spring Boot monolith now exclusively handles **OAuth2
authentication and Redis-backed sessions**, while the application domain
(profile, post, media, feed, notification) has been extracted into five
independent microservices that talk to each other over **gRPC** and
**RabbitMQ**.

The user-facing entry point is a **Spring Cloud Gateway** that routes
browser traffic to the right backend, and a **Vite / nginx** frontend that
proxies `/api/**` to the gateway. **MinIO** provides S3-compatible object
storage for post images, and **MySQL** stores relational data in isolated
schemas — one per service.

For the full architecture, see [architecture.md](architecture.md) and the
[architecture diagram](docs/ARCHITECTURE_DIAGRAM.md). The development plan
(what was broken in the legacy codebase, and the phased fix) is in
[plan.md](plan.md). All locked architectural decisions live in
[docs/DECISIONS.md](docs/DECISIONS.md).

---

## Table of contents

- [Architecture overview](#architecture-overview)
- [Login flow](#login-flow)
- [Local dev URLs](#local-dev-urls)
- [Environment variables](#environment-variables)
- [Quick start](#quick-start)
- [Project layout](#project-layout)
- [Documentation index](#documentation-index)

---

## Architecture overview

The platform is composed of **one monolith**, **five domain microservices**,
and **one API gateway**, fronted by a static React SPA. Every component is
written in Spring Boot 3 / Java 17 and orchestrated by `docker-compose`.

**Monolith (auth) — port 8081 (Docker) / 8080 (local).** The original
monolith (`social-network` Gradle module) has been gutted of business logic
and now does one job: terminate the **OAuth2** flow against Google, mint a
**Spring Session** cookie, and write the session payload into the shared
**Redis** instance. It also exposes a handful of `/auth/**` introspection
endpoints (e.g. `/auth/inspect`) that the SPA calls on load to learn who the
current user is.

**profile-service — HTTP 8084, gRPC 9092.** Owns the `profile_db` schema
(`profile`, `user_following` tables) and exposes a gRPC server so the other
services can hydrate usernames, avatars, and follow graphs without ever
touching another service's database.

**post-service — HTTP 8085, gRPC 9093.** Owns the `post_db` schema
(`post`, `comment`, `post_likes`). Stores `createdByProfileId` as a plain
foreign integer; author metadata is resolved on the fly via gRPC to
profile-service. After a successful `createPost`, post-service publishes
the new `postId` to a RabbitMQ queue for fan-out.

**media-service — HTTP 8083, gRPC 9091.** Accepts base64-encoded image
uploads, stores them in a **MinIO** bucket (`spring-boot`), and returns the
object key. The frontend prepends `VITE_MEDIA_BASE_URL` to render the
public URL. (See [ADR-001](docs/DECISIONS.md#adr-001-image-url-strategy).)

**feed-service — HTTP 8087.** Implements two parallel strategies. The
**dynamic feed** pulls the followings of the current user via gRPC, then
fetches recent posts on demand. The **precomputed feed** consumes
RabbitMQ `after-create-post-queue` events, looks up the author's followers,
and `LPUSH`es the new `postId` to a per-user Redis list
(`feed:{profileId}`) bounded to 1000 entries with a 30-day TTL (see
[ADR-011](docs/DECISIONS.md#adr-011-redis-feed-bounded)).

**notification-service — HTTP 8086.** Consumes RabbitMQ
`notification-event-queue`, hydrates the event via gRPC to profile-service,
persists a `Notification` row in `notification_db`, and pushes the
hydrated JSON to connected clients over **STOMP WebSocket**
(`/topic/notifications/{username}`).

**API gateway — port 8080.** The single HTTP entry point. Replaces legacy
nginx as the routing layer. Maps `/auth/**`, `/oauth2/**`, `/login/**`,
`/users/**` to the monolith, and `/media/**`, `/profiles/**`,
`/followers/**`, `/follow/**`, `/posts/**`, `/comments/**`,
`/dynamic-feeds/**`, `/precomputed-feeds/**`, `/notifications/**`,
`/gs-guide-websocket/**` to the matching microservice.

**Frontend — port 3000.** React 19 + Vite SPA. In dev mode the Vite server
proxies `/api/**` to `http://localhost:8080`; in production nginx
(`frontend/nginx.conf`) does the same against the `api-gateway` container.
All HTTP calls use `credentials: 'include'` so the session cookie is sent
on every request.

**Cross-cutting.** Every service connects to the same Redis instance under
the `engineerpro:app` namespace; the monolith writes the session and the
microservices read it. Service-to-service calls use **gRPC** (HTTP/2 +
protobuf) for synchronous, typed queries; **RabbitMQ** is reserved for
asynchronous fan-out and notification events.

---

## Login flow

End-to-end happy path (local hybrid mode — all stateful services in Docker,
gateway + frontend on the host):

1. The user opens the SPA at `http://localhost:3000`.
2. `App.tsx` calls `GET /api/auth/inspect`. The Vite proxy forwards the
   request to the gateway on `http://localhost:8080`.
3. The gateway matches `/auth/**` and routes to the monolith on
   `http://localhost:8081` (Docker host port — internally the monolith
   container listens on `8080`).
4. The monolith has no session cookie → returns `401` / `{authenticated:
   false}`.
5. The SPA shows a "Sign in" link that points to
   `http://localhost:8080/oauth2/authorization/google` (still going through
   the gateway, still routed to the monolith).
6. The user completes Google OAuth. The monolith's
   `OAuth2UserService` resolves the principal, builds a
   `UserPrincipal` (from `socialnetwork-common` — see
   [socialnetwork-common/README.md](socialnetwork-common/README.md)), and
   stores it in the session.
7. The monolith returns a `Set-Cookie: SESSION=...` header. Because the
   cookie was issued by the monolith **and** the browser is talking to the
   gateway, the browser sends the cookie on every subsequent request to
   `localhost:8080`.
8. The SPA re-calls `GET /api/auth/inspect`. The gateway forwards the
   request (with the cookie) to the monolith. The monolith reads the
   session from Redis, returns the user payload, and the SPA stores it in
   React context.
9. When the SPA calls `GET /api/profiles/me`, the gateway matches
   `/profiles/**` and routes to profile-service on
   `http://localhost:8084`. The browser still sends the session cookie
   because the path is the same. profile-service is configured with the
   same `@EnableRedisHttpSession(redisNamespace = "engineerpro:app")` and
   reads the session from Redis directly — no need to talk to the monolith.
10. The same pattern works for every other service. Notification
    WebSocket connections go through `/gs-guide-websocket/**` → gateway →
    notification-service; the STOMP handshake carries the session cookie,
    which is validated against Redis the same way.

> **Why this works.** The session lives in Redis, not in the monolith's
> memory. Any service that knows the `engineerpro:app` namespace and the
> shared `REDIS_PASSWORD` can read it. The cookie is scoped to the gateway
> host, so as long as everything is reached through the gateway, the cookie
> is sent. See [docs/DECISIONS.md](docs/DECISIONS.md) ADR-002 (canonical
> `UserPrincipal`) and ADR-005 (Redis-backed session) for the rationale.

---

## Local dev URLs

> Port numbers below are the **host-side** ports published by
> `docker-compose.yml`. In a pure local (no Docker) setup, all services
> listen on the same ports unless you override them — see
> [docs/LOCAL_DEV.md](docs/LOCAL_DEV.md).

| Service              | URL                              | Notes                                                    |
|----------------------|----------------------------------|----------------------------------------------------------|
| Frontend             | http://localhost:3000            | Vite dev server or nginx container (host port `3000`)    |
| API Gateway          | http://localhost:8080            | Spring Cloud Gateway — single HTTP entry point           |
| Monolith (auth)      | http://localhost:8081            | OAuth2 + session; local non-Docker monolith uses `8080`  |
| profile-service      | http://localhost:8084            | gRPC server `:9092`                                      |
| post-service         | http://localhost:8085            | gRPC server `:9093`                                      |
| media-service        | http://localhost:8083            | gRPC server `:9091` (uploads to MinIO)                   |
| feed-service         | http://localhost:8087            | gRPC client only (consumes `after-create-post-queue`)    |
| notification-service | http://localhost:8086            | gRPC client only; serves STOMP `/gs-guide-websocket`     |
| MinIO API            | http://localhost:9000            | S3-compatible object storage (bucket `spring-boot`)      |
| MinIO console        | http://localhost:9001            | `minioadmin` / `minioadmin`                              |
| RabbitMQ AMQP        | localhost:5672                   | `guest` / `guest` (dev)                                  |
| RabbitMQ STOMP       | localhost:61613                  | Same credentials; consumed by notification-service       |
| RabbitMQ management  | http://localhost:15672           | `guest` / `guest`                                        |
| MySQL                | localhost:3306                   | root password — see `.env.example`                       |
| Redis                | localhost:6379                   | Password — see `.env.example`                            |
| Swagger (via gateway)| http://localhost:8080/swagger-ui | If `springdoc` is enabled on the target service          |

> **Default credentials in dev** (override everything in `.env`):
> MySQL `app` / `app`, Redis password `devpassword`, MinIO
> `minioadmin` / `minioadmin`, RabbitMQ `guest` / `guest`.

---

## Environment variables

All variables have defaults baked into the Docker Compose file and the
`application.yml` of each service, so the platform starts without a `.env`
file. For a real deployment (or to enable Google login), copy
[`.env.example`](.env.example) to `.env` and fill in the values.

| Variable                 | Default                  | Used by                  | Notes                                                         |
|--------------------------|--------------------------|--------------------------|---------------------------------------------------------------|
| `GOOGLE_CLIENT_ID`       | `CHANGE_YOUR_*`          | Monolith (auth)          | Get from Google Cloud Console → APIs & Services → Credentials |
| `GOOGLE_CLIENT_SECRET`   | `CHANGE_YOUR_*`          | Monolith (auth)          | Same as above; **required for real OAuth login**              |
| `MYSQL_ROOT_PASSWORD`    | `app`                    | MySQL container          | Used to bootstrap the `MYSQL_USER` account                    |
| `MYSQL_USER`             | `app`                    | MySQL + all services     | App user for JPA connections                                  |
| `MYSQL_PASSWORD`         | `app`                    | MySQL + all services     | App user password                                             |
| `REDIS_PASSWORD`         | `devpassword`            | Redis + all services     | Required by all services for Spring Session                   |
| `MINIO_ROOT_USER`        | `minioadmin`             | MinIO container          | S3 access key                                                 |
| `MINIO_ROOT_PASSWORD`    | `minioadmin`             | MinIO container          | S3 secret key                                                 |
| `MINIO_BUCKET`           | `spring-boot`            | media-service, monolith  | Bucket name; pre-create or use the init script               |
| `RABBITMQ_DEFAULT_USER`  | `guest`                  | RabbitMQ container       | AMQP user                                                     |
| `RABBITMQ_DEFAULT_PASS`  | `guest`                  | RabbitMQ container       | AMQP password                                                 |
| `VITE_API_BASE_URL`      | `/api`                   | Frontend (Vite)          | Used as the base path for `fetch` calls                       |
| `VITE_MEDIA_BASE_URL`    | `http://localhost:9000/spring-boot` | Frontend (Vite) | Prepended to media object keys (see [ADR-001](docs/DECISIONS.md)) |

> **Never commit a real `.env` file.** It is in `.gitignore`. The
> `.env.example` template lists every variable with safe placeholders.

---

## Quick start

The fastest way to see the full platform running is Docker. See
[docs/LOCAL_DEV.md](docs/LOCAL_DEV.md) for hybrid and pure-local
alternatives.

```bash
# 1. Copy the env template
cp .env.example .env       # Mac/Linux
# Copy-Item .env.example .env   # PowerShell

# 2. Build and start everything
docker compose up -d --build

# 3. Open the app
#    http://localhost:3000            (frontend)
#    http://localhost:8080/swagger-ui/index.html   (gateway → swagger)
#    http://localhost:9001            (MinIO console)
#    http://localhost:15672           (RabbitMQ management)
```

Stop with `docker compose down` (keeps volumes) or
`docker compose down -v` (wipes everything).

For day-to-day development on a single service, the **hybrid mode**
(only the stateful services in Docker, gateway + the service you're
working on via `./gradlew :service:bootRun`) is much faster — see
[docs/LOCAL_DEV.md § Option B](docs/LOCAL_DEV.md).

---

## Project layout

```
.
├── api-gateway/          # Spring Cloud Gateway (:8080)
├── feed-service/         # Feed aggregation (:8087)
├── frontend/             # React 19 + Vite SPA (:3000)
├── media-service/        # MinIO uploads (:8083, gRPC :9091)
├── notification-service/ # WebSocket notifications (:8086)
├── post-service/         # Posts + comments (:8085, gRPC :9093)
├── profile-service/      # Profiles + follows (:8084, gRPC :9092)
├── proto-contracts/      # Shared .proto files for gRPC
├── socialnetwork-common/ # Shared DTOs, config, security
├── src/                  # Monolith (auth) module
├── docs/                 # All documentation (see index below)
├── docker-compose.yml
├── Dockerfile            # Monolith build
├── *.Dockerfile          # Per-service builds live in each module
├── mysql-init.sql
├── plan.md               # Phased fix roadmap
├── architecture.md       # Architectural reference
└── .env.example
```

---

## Documentation index

| Document | Purpose |
|---|---|
| [architecture.md](architecture.md) | High-level architecture and tech stack reference |
| [plan.md](plan.md) | Phased fix roadmap (the "why" behind the current shape of the code) |
| [docs/ARCHITECTURE_DIAGRAM.md](docs/ARCHITECTURE_DIAGRAM.md) | Mermaid diagram of services, queues, and gRPC links |
| [docs/DECISIONS.md](docs/DECISIONS.md) | 14 locked architectural decisions (ADRs) |
| [docs/queues.md](docs/queues.md) | RabbitMQ exchanges, queues, producers, consumers |
| [docs/LOCAL_DEV.md](docs/LOCAL_DEV.md) | How to run locally (Docker, hybrid, pure local), profiles, troubleshooting |
| [docs/CHANGELOG.md](docs/CHANGELOG.md) | Versioned changelog |
| [CONTRIBUTING.md](CONTRIBUTING.md) | Code style, branch / commit conventions, PR process |
| [socialnetwork-common/README.md](socialnetwork-common/README.md) | Shared library rules (canonical `UserPrincipal`, `BaseResponse`, `SessionConfig`) |
