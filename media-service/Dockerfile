FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper and build files from media-service root
COPY gradlew gradlew.bat ./
COPY gradle gradle/
COPY build.gradle settings.gradle ./

# Copy proto-contracts module (dependency)
COPY proto-contracts ../proto-contracts/

# Copy media-service source
COPY media-service/src src/

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8091 9091

ENTRYPOINT ["java", "-jar", "app.jar"]
