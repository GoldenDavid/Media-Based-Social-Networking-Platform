#!/usr/bin/env bash
# Smoke test for the Media Social Platform Docker stack.
# Brings up the full stack, polls /actuator/health on every service,
# exercises a few public endpoints, then tears everything down.
#
# Usage:
#   ./scripts/smoke-test.sh
#
# Requires: bash, docker (compose v2), curl, jq (optional).
# Exits non-zero on first failure so it is CI-friendly.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

COMPOSE="docker compose"
if ! docker compose version >/dev/null 2>&1; then
  COMPOSE="docker-compose"
fi

HEALTH_TIMEOUT_S=180
POLL_INTERVAL_S=3

# ── Helpers ──────────────────────────────────────────────────────────────

green() { printf "\033[32m%s\033[0m\n" "$*"; }
red()   { printf "\033[31m%s\033[0m\n" "$*"; }
yello() { printf "\033[33m%s\033[0m\n" "$*"; }

step()   { yello "── $* ──"; }
ok()     { green "✓ $*"; }
fail()   { red   "✗ $*"; exit 1; }

wait_health() {
  local name="$1" url="$2"
  local elapsed=0
  step "Waiting for $name to be healthy at $url"
  while (( elapsed < HEALTH_TIMEOUT_S )); do
    code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "$url" || echo "000")
    if [[ "$code" == "200" ]]; then
      ok "$name is UP ($url)"
      return 0
    fi
    sleep "$POLL_INTERVAL_S"
    elapsed=$(( elapsed + POLL_INTERVAL_S ))
  done
  fail "$name did not become healthy within ${HEALTH_TIMEOUT_S}s (last code=$code)"
}

# ── Boot ────────────────────────────────────────────────────────────────

step "Bringing up the stack"
$COMPOSE up -d --wait --remove-orphans

# ── Service health checks (host-side ports) ────────────────────────────

wait_health "gateway (8080)"         "http://localhost:8080/actuator/health"
wait_health "monolith (8081)"        "http://localhost:8081/actuator/health"
wait_health "media-service (8083)"   "http://localhost:8083/actuator/health"
wait_health "profile-service (8084)" "http://localhost:8084/actuator/health"
wait_health "post-service (8085)"    "http://localhost:8085/actuator/health"
wait_health "notification (8086)"    "http://localhost:8086/actuator/health"
wait_health "feed-service (8087)"    "http://localhost:8087/actuator/health"
wait_health "MinIO (9000)"           "http://localhost:9000/minio/health/live"
wait_health "RabbitMQ (15672)"       "http://localhost:15672/api/overview"

# ── Public endpoint smoke (no auth) ────────────────────────────────────

step "Verifying public gateway endpoints"

# /auth/inspect should return 401 (not authenticated) on a fresh stack
code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 \
  "http://localhost:8080/auth/inspect")
[[ "$code" == "401" ]] || fail "GET /auth/inspect expected 401, got $code"
ok "GET /auth/inspect -> 401 (as expected)"

# Feed endpoints are permitAll() and should return 200 with an empty feed
code=$(curl -s -o /tmp/feed.json -w "%{http_code}" --max-time 5 \
  "http://localhost:8080/dynamic-feeds?page=1&limit=10")
[[ "$code" == "200" ]] || fail "GET /dynamic-feeds expected 200, got $code"
ok "GET /dynamic-feeds -> 200"
if command -v jq >/dev/null; then
  jq -e '.posts' /tmp/feed.json >/dev/null \
    || fail "/dynamic-feeds response has no .posts key"
  jq -e '.totalPage' /tmp/feed.json >/dev/null \
    || fail "/dynamic-feeds response has no .totalPage key"
  ok "/dynamic-feeds payload has posts + totalPage"
fi

# Frontend is served by Nginx, not gateway
step "Verifying frontend nginx"
code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 \
  "http://localhost:3000/" || echo "000")
if [[ "$code" == "200" ]]; then
  ok "frontend (3000) -> 200"
else
  yello "frontend (3000) not reachable (code=$code). Skipping."
fi

# ── Tear down ──────────────────────────────────────────────────────────

step "Tearing down the stack"
$COMPOSE down -v

green "════════════════════════════════════════════════════════════════"
green "  SMOKE TEST PASSED"
green "════════════════════════════════════════════════════════════════"
