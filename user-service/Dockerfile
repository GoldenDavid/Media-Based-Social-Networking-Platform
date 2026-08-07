# syntax=docker/dockerfile:1.6

FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /src

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY socialnetwork-common socialnetwork-common
COPY user-service user-service

RUN sed -i 's/\r$//' ./gradlew \
 && chmod +x ./gradlew \
 && ./gradlew :user-service:bootJar --no-daemon -x test \
 && ls -la user-service/build/libs/

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl tini ca-certificates \
 && rm -rf /var/lib/apt/lists/*

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8

RUN groupadd --system app && useradd --system --gid app --home /app app

WORKDIR /app
COPY --from=build /src/user-service/build/libs/*-1.0.jar /app/app.jar

ENV SERVER_PORT=8081

EXPOSE 8081
USER app

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=5 \
  CMD curl -fsS http://localhost:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["/usr/bin/tini", "--", "java", "-jar", "/app/app.jar"]
