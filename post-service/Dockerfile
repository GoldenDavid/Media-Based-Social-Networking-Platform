# syntax=docker/dockerfile:1.6

# ── Stage 1: build the boot jar ─────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /src

# Copy the wrapper + settings first so dependency resolution is cached
# when only service source changes.
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY socialnetwork-common socialnetwork-common
COPY proto-contracts proto-contracts
COPY post-service post-service

# Strip CRLF from gradlew (Windows checkouts keep CRLF line endings which
# break the shebang in Linux containers) and make it executable.
RUN sed -i 's/\r$//' ./gradlew \
 && chmod +x ./gradlew \
 && ./gradlew :post-service:bootJar --no-daemon -x test \
 && ls -la post-service/build/libs/

# ── Stage 2: minimal JRE runtime ───────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

# curl is preinstalled on jammy but be explicit for clarity in the healthcheck.
# tini gives us proper PID 1 signal handling (graceful shutdown on SIGTERM).
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl tini ca-certificates \
 && rm -rf /var/lib/apt/lists/*

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8

RUN groupadd --system app && useradd --system --gid app --home /app app

WORKDIR /app
COPY --from=build /src/post-service/build/libs/*-1.0.jar /app/app.jar

ENV SERVER_PORT=8085 \
    GRPC_PORT=9093

EXPOSE 8085 9093
USER app

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=5 \
  CMD curl -fsS http://localhost:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["/usr/bin/tini", "--", "java", "-jar", "/app/app.jar"]
