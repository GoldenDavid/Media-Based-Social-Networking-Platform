# Architecture Diagram

> Visual map of the runtime topology. The Mermaid block below renders
> in GitHub, GitLab, and most Markdown previews. To re-render it outside
> GitHub, paste it into <https://mermaid.live>.

The diagram mirrors the prose in [`README.md`](../README.md) and
[`architecture.md`](../architecture.md); treat this file as the
single-page cheat sheet.

## Runtime topology

```mermaid
flowchart LR
    %% Edge / browser
    Browser["Browser / SPA<br/>(React 19 + Vite)"]

    %% Frontend container
    subgraph FE ["Frontend (nginx :80)"]
        Nginx["nginx :80<br/>/api/** → gateway :8080"]
    end

    %% Gateway
    subgraph GW ["API Gateway :8080 (Spring Cloud Gateway)"]
        Gateway["Routes:<br/>/auth/**, /oauth2/**, /login/**, /users/** → monolith<br/>/media/** → media-service<br/>/profiles/**, /followers/**, /follow/** → profile-service<br/>/posts/**, /comments/** → post-service<br/>/dynamic-feeds/**, /precomputed-feeds/** → feed-service<br/>/notifications/**, /gs-guide-websocket/** → notification-service"]
    end

    %% Auth
    subgraph AUTH ["Monolith / Auth :8080→8081 (Docker)"]
        Monolith["OAuth2 (Google)<br/>Spring Session<br/>BaseResponse / UserPrincipal"]
    end

    %% Microservices
    subgraph MS ["Domain microservices"]
        Profile["profile-service<br/>HTTP :8084 / gRPC :9092<br/>DB: profile_db"]
        Post["post-service<br/>HTTP :8085 / gRPC :9093<br/>DB: post_db"]
        Media["media-service<br/>HTTP :8083 / gRPC :9091<br/>MinIO bucket: spring-boot"]
        Feed["feed-service<br/>HTTP :8087<br/>Dynamic + Precomputed feeds"]
        Notification["notification-service<br/>HTTP :8086<br/>STOMP /gs-guide-websocket<br/>DB: notification_db"]
    end

    %% Data
    subgraph DATA ["Stateful infrastructure"]
        Redis[("Redis :6379<br/>namespace: engineerpro:app<br/>session + feed:{profileId}")]
        MySQL[("MySQL :3306<br/>schemas: user_db,<br/>profile_db, post_db,<br/>notification_db")]
        MinIO[("MinIO :9000 / :9001<br/>bucket: spring-boot")]
        RabbitMQ[["RabbitMQ :5672<br/>+ STOMP :61613<br/>+ mgmt :15672"]]
    end

    %% Edges
    Browser -->|/api/**| Nginx
    Nginx -->|HTTP| Gateway
    Gateway -->|HTTP| Monolith
    Gateway -->|HTTP| Profile
    Gateway -->|HTTP| Post
    Gateway -->|HTTP| Media
    Gateway -->|HTTP| Feed
    Gateway -->|HTTP/WS| Notification

    %% Session + state
    Monolith <-->|session read/write| Redis
    Profile <-->|session read| Redis
    Post <-->|session read| Redis
    Feed <-->|session read + feed cache| Redis
    Notification <-->|session read| Redis

    %% gRPC sync
    Post -.->|gRPC GetProfile| Profile
    Post -.->|gRPC UploadImage| Media
    Profile -.->|gRPC GetMedia| Media
    Feed -.->|gRPC GetFollowings / GetProfile| Profile
    Feed -.->|gRPC GetPostsByIds| Post
    Notification -.->|gRPC GetProfile| Profile

    %% RabbitMQ async
    Post ==>|after-create-post-queue<br/>(int postId)| RabbitMQ
    RabbitMQ ==>|consume| Feed
    Post ==>|notification-event-queue<br/>(JSON, TBD)| RabbitMQ
    RabbitMQ ==>|consume| Notification
    RabbitMQ -.->|notification-event-queue.dlq<br/>(dead-letter)| RabbitMQ

    %% Databases
    Profile <--> MySQL
    Post <--> MySQL
    Notification <--> MySQL
    Monolith <--> MySQL
    Media <--> MinIO
```

## Reading the diagram

- **Solid arrows** = synchronous HTTP / gRPC.
- **Dashed arrows** = synchronous gRPC inter-service calls.
- **Thick double arrows** = asynchronous RabbitMQ traffic.
- **Boxes** = a single process. **Subgraphs** group them by role
  (frontend, gateway, monolith, microservices, data plane).
- **Cylinders / drums** = stateful components (Redis, MySQL, MinIO,
  RabbitMQ).

## Port summary

| Component             | HTTP | gRPC | Notes                                         |
|-----------------------|------|------|-----------------------------------------------|
| Frontend (Vite / nginx) | 3000 / 80 | — | Proxies `/api/**` to the gateway              |
| API Gateway           | 8080 | —    | Spring Cloud Gateway                          |
| Monolith (auth)       | 8081 (Docker host) / 8080 (local) | — | OAuth2 + session                                |
| profile-service       | 8084 | 9092 | `profile_db`                                  |
| post-service          | 8085 | 9093 | `post_db`                                     |
| media-service         | 8083 | 9091 | MinIO bucket `spring-boot`                    |
| feed-service          | 8087 | —    | gRPC client only; consumes `after-create-post-queue` |
| notification-service  | 8086 | —    | STOMP consumer; serves `/gs-guide-websocket`  |
| MySQL                 | 3306 | —    | One schema per service                        |
| Redis                 | 6379 | —    | Session namespace `engineerpro:app`           |
| MinIO                 | 9000 (API) / 9001 (console) | — | `minioadmin` / `minioadmin`                  |
| RabbitMQ              | 5672 (AMQP) / 61613 (STOMP) / 15672 (mgmt) | — | `guest` / `guest` |

> If a port changes, update this file **and** the README's
> [Local dev URLs](../README.md#local-dev-urls) table in the same PR.
