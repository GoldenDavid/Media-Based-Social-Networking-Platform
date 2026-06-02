# socialnetwork-common

Shared Java library for cross-service code (DTOs, config, security).

## Why this module exists

Before this module was created, the same classes were duplicated across
the monolith and the microservices, with subtle differences that caused
runtime failures. The most damaging was the `UserPrincipal` mismatch:
the monolith wrote a Redis session blob without Jackson's `@JsonTypeInfo`
discriminator, but the microservices required it on read, causing every
authenticated request from a microservice to throw
`MismatchedInputException`.

## What lives here

| Class | Package | Purpose |
|---|---|---|
| `UserPrincipal` | `com.socialnetwork.common.security` | The single, canonical `Authentication` principal used by every service |
| `BaseResponse<T>` | `com.socialnetwork.common.dto` | The standard API response envelope |
| `SessionConfig` | `com.socialnetwork.common.config` | The Spring Session + cookie config that every service must use |

## Rules

1. **Do not duplicate** any class from this module in any other service. Import from here instead.
2. **Do not add** application logic to this module. It is for cross-cutting types only.
3. **Do not add** Spring Boot auto-configuration to this module. Each service picks up the beans it needs.
4. **The FQCN of `UserPrincipal` is part of the Redis session format.** If you ever need to move the class, you must invalidate every active session.

## How to add a new service

```groovy
// in your-service/build.gradle
dependencies {
    implementation project(':socialnetwork-common')
    // ... other dependencies
}
```

```java
// in your-service/src/main/java/.../YourApplication.java
@SpringBootApplication(scanBasePackages = {
    "com.socialnetwork.your.service",
    "com.socialnetwork.common"
})
```
