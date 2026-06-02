# Changelog

All notable changes to the Media-Based Social Networking Platform.

Format: [Keep a Changelog](https://keepachangelog.com/).
Versioning: [SemVer](https://semver.org/).

---

## [Unreleased]

### Added
- Home page now has a Dynamic / Precomputed feed-source toggle
  (Phase 5.4 UI). The choice persists in `localStorage` under
  `app:feedSource`. Per ADR-018, dynamic is the default; the
  precomputed feed (Redis fan-out cache, may lag by seconds) is
  opt-in. Both endpoints return a flat `GetFeedResponse` shape.
- New test in `frontend/src/services/api.test.ts` pins the
  flat-shape contract for both feeds so a future backend
  `BaseResponse` wrap is caught immediately.
- `LIKE_YOUR_POST` and `COMMENT_YOUR_POST` notification producers
  (Phase 5.7). `PostServiceImpl.likePost` publishes a
  `NotificationEvent` to the post's author; `CommentServiceImpl.createComment`
  does the same. Both are suppressed when the actor is the post's
  author. The `notification-event-queue` topology is now complete
  for all three `NotificationType` values (`NEW_POST` was wired in
  Phase 1.5).
- Notification history endpoint `GET /api/notifications/me?page=&limit=`
  (Phase 5.3). Returns the persisted notification history for the
  authenticated user, most recent first. The frontend
  `NotificationDrawer` now loads history the first time it opens and
  merges it with live STOMP pushes (deduplicated by `id`).
- New `NotificationService` and `NotificationController` in
  notification-service. The `profile-service` gRPC client in
  notification-service gained a `getProfile(UserPrincipal)` overload
  to translate the session UUID into a numeric `profileId` (the
  notification table is keyed on the int).
- NotificationEventPersistenceTest: `@DataJpaTest` slice that asserts
  the JPA storage contract (the `serialVersionUID` on
  `NotificationEvent` is also pinned).
- Phase 5.4 documented: default feed is `/dynamic-feeds`; the
  precomputed feed is opt-in (UI toggle pending).
- Profile page shows real follower / following counts (Phase 1.6).
  `api.getFollowers` / `api.getFollowings` hit the
  `/follow/user/followers/{id}` and `/follow/user/followings/{id}`
  endpoints; counts are displayed next to the post count, or `0` if
  the call fails on a fresh account.

### Fixed
- `profile-service` `FollowerController` responses now use typed
  `BaseResponse<FollowersResponse>` / `BaseResponse<FollowingsResponse>`
  / `BaseResponse<FollowResponse>` (Phase 1.6) instead of the awkward
  `Map.of("data", Map.of("totalPage", ..., "followers", ...))` shape
  that shadowed the `BaseResponse.data` field.
- `profile-service` `/profiles/me`, `/profiles/{id}`, `POST /profiles`,
  `POST /profiles/profile-image` now wrap their responses in
  `BaseResponse<Map<String, ...>>` (Phase 1.5) so the frontend
  `unwrap()` helper returns the expected `profile` / `url` fields.
- `notification-event-queue` now has a real producer:
  `post-service` publishes a `NotificationEvent(type=NEW_POST)` to every
  follower of the post author on `createPost()`. The event class
  lives in `com.socialnetwork.common.event` (was in
  `notification-service`) so the publisher and consumer share one
  classloader (required for JDK serialization).
- FE test `alex_cyber` literal replaced with `alice_dev` to align
  with the seed data convention (Phase 5.6).
  NotificationService's `MessageQueueConfig` still owns the queue
  and DLX declaration; the post-service owns the
  `MessageQueueConfig.NOTIFICATION_EVENT_QUEUE` constant for routing.

### Changed
- `scripts/smoke-test.sh` and `scripts/smoke-test.ps1` added (Phase 1.5):
  bring up the full Docker stack, poll `/actuator/health` on every
  service, verify a couple of public endpoints, then tear down. CI-ready.

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
