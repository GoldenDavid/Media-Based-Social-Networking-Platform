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
