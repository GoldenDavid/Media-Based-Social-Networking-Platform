FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache wget && addgroup -S app && adduser -S app -G app
USER app

VOLUME /tmp

ARG MODULE_NAME=post-service
COPY build/libs/${MODULE_NAME}-1.0.jar app.jar

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -q -O /dev/null http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app.jar"]
