# Changelog

All notable changes to the Media-Based Social Networking Platform.

Format: [Keep a Changelog](https://keepachangelog.com/).
Versioning: [SemVer](https://semver.org/).

---

## [Unreleased]

### Added
- `docs/DECISIONS.md` with 12 locked architecture decisions.
- `docs/queues.md` documenting the RabbitMQ topology.
- `docs/LOCAL_DEV.md` with local development instructions.
- `socialnetwork-common` Gradle module: shared DTOs (`UserPrincipal`, `BaseResponse`) and config (`SessionConfig`).
- `docs/CHANGELOG.md` (this file).

### Changed
- Refactored `UserPrincipal` and `BaseResponse` into `socialnetwork-common` (single source of truth).
- All services now `implementation project(':socialnetwork-common')`.

### Fixed
- Documentation gap: `architecture.md` and `plan.md` were git-ignored; now mirrored by `docs/`.

---

## [0.1.0] — Initial monolith

- Spring Boot 3.1.5 monolith with OAuth2 (Google), JPA (MySQL), Redis sessions, MinIO uploads, RabbitMQ, WebSocket (STOMP).
- Basic profile/post/comment/feed/follow flow.
- React 19 + Vite frontend with Auth, Home, Profile, Create Post, Notification Drawer.

### Known issues (fixed in Unreleased)
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
