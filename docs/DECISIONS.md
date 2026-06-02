# Architecture Decisions

> Locked decisions for the Media-Based Social Networking Platform. Each decision is **binding** for the project. Any deviation must update this file in the same PR.

---

## ADR-001: Image URL strategy

**Status:** Accepted (Phase 0)
**Context:** Backend `media-service` returns object keys; frontend hardcoded `http://localhost:9000/spring-boot`.
**Decision:** Backend returns the object key only. Frontend prepends `VITE_MEDIA_BASE_URL` from environment.
**Consequences:**
- Frontend can deploy against any MinIO endpoint via env var.
- Backend stays pure (no public hostname knowledge).
- A future Phase 4 change may add presigned URLs; in that case the response shape changes and the FE must update together.

## ADR-002: `Map.of` → typed DTOs

**Status:** Accepted (Phase 0)
**Context:** `Map.of("profile", ...)` throws NPE on null values. The endpoint is fragile.
**Decision:** All controller responses use typed DTOs (`ProfileResponse`, `FollowersResponse`, etc.) with `@JsonInclude(NON_NULL)`.
**Consequences:**
- Refactor-safe. `null` fields are serialized as `null`, not as exceptions.
- Stable FE contract.
- Slight verbosity in DTO count.

## ADR-003: Base64 prefix preserved

**Status:** Accepted (Phase 0)
**Context:** Frontend `CreatePost.tsx` strips `data:image/...;base64,` prefix; backend `parseExtension` requires it for MIME detection.
**Decision:** Frontend sends the full data URI (no prefix strip).
**Consequences:**
- Zero backend change.
- Backend `parseExtension` correctly identifies PNG/GIF/WebP.
- Frontend comment "backend expects pure base64" is wrong — corrected in code.

## ADR-004: Tests use Testcontainers + JUnit 5

**Status:** Accepted (Phase 0)
**Context:** Project had `spring-boot-starter-test` but zero tests.
**Decision:** Integration tests use real MySQL/Redis/RabbitMQ via Testcontainers. Unit tests use JUnit 5 + Mockito.
**Consequences:**
- Tests reflect production behavior.
- Slower test execution; mitigated by running unit tests separately.
- CI needs Docker (already required).

## ADR-005: Migrations use Flyway

**Status:** Accepted (Phase 0)
**Context:** All services use `spring.jpa.hibernate.ddl-auto: update` (fragile, prod-unsafe).
**Decision:** Add Flyway to every service. Replace `update` with `validate`. One `V1__init.sql` per service.
**Consequences:**
- Schema changes are versioned and auditable.
- Existing data migration is documented per service.
- Fresh installs: `V1__init.sql` creates the full schema.

## ADR-006: Frontend types generated from OpenAPI

**Status:** Accepted (Phase 0)
**Context:** Hand-written FE types drift from BE responses. Multiple `ProfileDto` classes across services with different fields.
**Decision:** Every Java service exposes springdoc OpenAPI. Frontend uses `openapi-typescript-codegen` to generate `src/types/api.d.ts` from the monolith's `/v3/api-docs`.
**Consequences:**
- CI fails if generated types are out of date.
- Drift is detected at build time.
- Microservice-specific contracts require hitting each service's `/v3/api-docs`.

## ADR-007: Observability stack

**Status:** Accepted (Phase 0)
**Context:** No metrics, no tracing, logs to local file.
**Decision:** Add Prometheus (metrics), Grafana (dashboards), Loki (logs), OpenTelemetry (tracing). All orchestrated via docker-compose.
**Consequences:**
- Every Java service exposes `/actuator/prometheus`.
- Tracing adds 5–10% overhead; disabled in dev profile.
- Operability improves; storage cost increases (mitigated by short retention in dev).

## ADR-008: CI uses GitHub Actions

**Status:** Accepted (Phase 0)
**Context:** No CI; every change is manual.
**Decision:** GitHub Actions: `./gradlew build -x test` + `npm run build` on every PR; full E2E on main.
**Consequences:**
- Standard, free for public repos.
- Requires Docker for E2E jobs.
- E2E secrets via GitHub Secrets.

## ADR-009: Like race fixed via unique constraint

**Status:** Accepted (Phase 0)
**Context:** `PostServiceImpl.likePost` race condition on concurrent likes.
**Decision:** Add `POST_LIKES` table with `UNIQUE(post_id, profile_id)`. Use `INSERT ... ON DUPLICATE KEY UPDATE` for idempotency.
**Consequences:**
- Real fix at the DB layer.
- No retry logic needed.
- Schema migration required (Flyway).

## ADR-010: N+1 gRPC resolved via batch RPC

**Status:** Accepted (Phase 0)
**Context:** `PostServiceImpl.toPostDto` makes 1 gRPC call per post per comment per liker.
**Decision:** Add `GetProfilesByIds` batch RPC to `profile.proto`. All services use it instead of single-call.
**Consequences:**
- Proto change ripples to all consumers.
- Order preserved (response reordered by request).
- Migration done as a single PR.

## ADR-011: Redis feed bounded

**Status:** Accepted (Phase 0)
**Context:** `FeedRepository.leftPush` is unbounded → Redis OOM in production.
**Decision:** `LTRIM 0 999` and `EXPIRE 30d` on every feed write.
**Consequences:**
- Bounded memory.
- Older posts drop out of precomputed feed.
- Dynamic feed still has unlimited history.

## ADR-012: Demo data is part of the deliverable

**Status:** Accepted (Phase 0)
**Context:** A fresh install has 0 users, 0 posts. Demo requires seed.
**Decision:** `seed.sh` + `seed.ps1` create 3 users (alice, bob, carol) with follows, posts, comments. Idempotent (checks before insert).
**Consequences:**
- Demo works out of the box.
- Seed needs a valid auth flow (depends on Phase 1).
- OAuth2 user is mocked (no real Google login required for demo).

## ADR-013: Phase 1 lane work — shared `UserPrincipal` + shared `SessionConfig`

**Status:** Accepted (Phase 1)
**Context:** Before Phase 1, the same `UserPrincipal` class was duplicated
in the monolith (`src/main/java/com/socialnetwork/dto/UserPrincipal.java`)
and in `post-service` (`post-service/.../dto/UserPrincipal.java`), with
subtly different Jackson annotations. The monolith wrote a Redis session
blob without `@JsonTypeInfo`; the microservices required it on read. The
result was a `MismatchedInputException` on every authenticated call that
reached a microservice. The same problem applied to `BaseResponse` and to
the `SessionConfig` (`@EnableRedisHttpSession` + cookie hardening).
**Decision:**
- Create the `socialnetwork-common` Gradle module with a single canonical
  `UserPrincipal` (implements `OAuth2User` + `UserDetails`), a single
  `BaseResponse<T>` envelope, and a single `SessionConfig` that all
  consuming services `@Import`.
- Every service `implementation project(':socialnetwork-common')` and
  scans `com.socialnetwork.common` alongside its own package.
- The Redis session namespace is fixed at `engineerpro:app` for every
  service (monolith writer + microservice reader).
- The fully-qualified class name of `UserPrincipal` is now part of the
  Redis session payload format; **renaming or moving the class requires
  invalidating every active session**.
**Consequences:**
- Zero duplication of session / DTO / principal code.
- A change to the principal propagates to every service in one PR.
- New services must depend on `:socialnetwork-common`; the alternative
  (copy-paste) is explicitly forbidden (see
  [`socialnetwork-common/README.md`](../socialnetwork-common/README.md)).
- Pre-Phase-1 sessions in Redis are unreadable (different FQCN) — they
  expire on the 30-day TTL or are flushed on `docker compose down -v`.

## ADR-014: Frontend `BASE_URL = /api` + `credentials: 'include'`

**Status:** Accepted (Phase 1)
**Context:** The frontend's `api.ts` was hardcoded to
`http://localhost:8080`, did not send cookies, and ignored the `/api`
proxy. This worked in local dev with CORS hand-waving but broke in
Docker, broke behind Vite's proxy, and bypassed the session cookie on
every microservice call.
**Decision:**
- `BASE_URL = '/api'` in `frontend/src/services/api.ts`.
- Every `fetch` / `axios` call uses `credentials: 'include'`.
- Vite dev server (`frontend/vite.config.ts`) and the production nginx
  container (`frontend/nginx.conf`) both proxy `/api/**` to the gateway,
  stripping the `/api` prefix and forwarding `Set-Cookie` (rewriting
  `Secure` / `Domain` in dev).
- The frontend parses the `BaseResponse<T>` envelope for endpoints that
  return one (post, profile) and reads `data.posts` / `data.profile` etc.
  directly for endpoints that don't (feed).
**Consequences:**
- The browser always sees the SPA origin (`localhost:3000` or
  `frontend:80`), so the session cookie scope is consistent.
- The Vite proxy and nginx config become part of the API contract —
  changes to either file are reviewed in the same PR as the gateway.
- A direct `fetch('http://localhost:8080/...')` from a component is now
  a bug; lint rules will catch it (see
  [`CONTRIBUTING.md`](../CONTRIBUTING.md)).

---

## Decision log

| Date | ADR | Title | Status |
|---|---|---|---|
| Phase 0 | 001 | Image URL strategy | Accepted |
| Phase 0 | 002 | `Map.of` → typed DTOs | Accepted |
| Phase 0 | 003 | Base64 prefix preserved | Accepted |
| Phase 0 | 004 | Tests use Testcontainers | Accepted |
| Phase 0 | 005 | Migrations use Flyway | Accepted |
| Phase 0 | 006 | Frontend types from OpenAPI | Accepted |
| Phase 0 | 007 | Observability stack | Accepted |
| Phase 0 | 008 | CI uses GitHub Actions | Accepted |
| Phase 0 | 009 | Like race via unique constraint | Accepted |
| Phase 0 | 010 | N+1 gRPC via batch RPC | Accepted |
| Phase 0 | 011 | Redis feed bounded | Accepted |
| Phase 0 | 012 | Demo data is part of the deliverable | Accepted |
| Phase 1 | 013 | Shared `UserPrincipal` + shared `SessionConfig` (socialnetwork-common) | Accepted |
| Phase 1 | 014 | Frontend `BASE_URL = /api` + `credentials: 'include'` | Accepted |
