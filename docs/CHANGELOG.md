# Changelog

All notable changes to the Media-Based Social Networking Platform.

Format: [Keep a Changelog](https://keepachangelog.com/).
Versioning: [SemVer](https://semver.org/).

---

## [Unreleased]

### Added
- _None yet — next batch lands under 0.3.0 once Phase 5 features ship._

---

## [0.2.0] — Phase 0 + Phase 1 (foundation + auth + config)

### Added
- `docs/DECISIONS.md` with 12 locked Phase 0 ADRs and 2 new Phase 1 ADRs
  (`013` shared `UserPrincipal` / `SessionConfig`, `014` frontend
  `BASE_URL = /api` + `credentials: 'include'`).
- `docs/queues.md` documenting the RabbitMQ topology, including the new
  `notification-event-queue.dlq` dead-letter queue.
- `docs/LOCAL_DEV.md` with local development instructions, profile
  reference, and a troubleshooting section for the most common
  Docker / Redis / OAuth / gRPC issues.
- `docs/CHANGELOG.md` (this file).
- `CONTRIBUTING.md` (at repo root) with code style, branch / commit
  conventions, and PR process.
- `docs/ARCHITECTURE_DIAGRAM.md` with a Mermaid diagram of services,
  queues, and gRPC links.
- `socialnetwork-common` Gradle module: shared DTOs (`UserPrincipal`,
  `BaseResponse`) and config (`SessionConfig`). See
  [`socialnetwork-common/README.md`](../socialnetwork-common/README.md).
- `architecture.md` and `plan.md` are now tracked in git (previously
  git-ignored). They are part of the source of truth for the project.
- New `notification-event-queue.dlq` declared by `notification-service`
  (Phase 1 — see ADR-013 for the shared-session rationale).

### Changed
- Refactored `UserPrincipal`, `BaseResponse`, and `SessionConfig` into
  `socialnetwork-common` (single source of truth).
- All services now `implementation project(':socialnetwork-common')` and
  scan `com.socialnetwork.common` alongside their own package.
- Frontend `api.ts` now uses `BASE_URL = '/api'` and
  `credentials: 'include'` on every request (was: hardcoded
  `http://localhost:8080`).
- Vite dev server (`frontend/vite.config.ts`) and production nginx
  (`frontend/nginx.conf`) proxy `/api/**` to the gateway and rewrite
  `Secure` / `Domain` cookie attributes for local HTTP development.
- `README.md` rewritten from scratch — now includes the architecture
  overview, the end-to-end login flow, a local-dev URL table, and an
  env-var reference.

### Fixed
- Documentation gap: `architecture.md` and `plan.md` were git-ignored;
  they are now part of the repository so collaborators can see them on
  the remote.
- Legacy reference to `nginx/conf/app.conf` removed from the README; the
  entry point is now the **API gateway on `:8080`** with the frontend on
  `:3000` proxying `/api` to it.
- Mismatched port assignments in the architecture map (media-service
  vs notification-service) corrected in the README URL table.

### Known follow-ups
- `notification-event-queue` has no producer yet — see
  [docs/queues.md § Migration notes](queues.md#migration-notes).
- Phase 5 features (Create Post page, persisted notifications, test
  coverage) are tracked in [plan.md § Phase 5](../plan.md).

---

## [0.1.0] — Initial monolith

- Spring Boot 3.1.5 monolith with OAuth2 (Google), JPA (MySQL), Redis sessions, MinIO uploads, RabbitMQ, WebSocket (STOMP).
- Basic profile/post/comment/feed/follow flow.
- React 19 + Vite frontend with Auth, Home, Profile, Create Post, Notification Drawer.

### Known issues (fixed in 0.2.0)
- OAuth2 login not enabled in `SecurityConfig`.
- OAuth2-registered users cannot log in (`enabled=false` default).
- Session cookie marked `Secure` over plain HTTP.
- `UserPrincipal` Jackson annotation mismatch between monolith and microservices.
- `Map.of("profile", ...)` NPE on null fields.
- CORS spec violation (`allowedHeaders: "*"` with credentials).
- `Map.of` fragility and `permitAll` security bypass in `post-service` / `feed-service`.
- STOMP WebSocket has no auth interceptor.
- Notification consumer listens to a queue with no producer.
- Image URL returns object key, FE hardcodes localhost.
- MinIO bucket is world-readable; no auth on `media-service` gRPC.
- Docker: no named volumes; no UTF-8 charset; gateway route prefix not stripped.
- And many more — see `plan.md` (legacy) and `docs/DECISIONS.md` for the full list.
