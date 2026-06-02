# RabbitMQ Queue Topology

> Single source of truth for all exchanges, queues, bindings, producers, and consumers. **Update this file in the same PR as any code that adds, removes, or renames a queue.**

---

## Brokers

| Broker | Host | Port | STOMP Port | Credentials |
|---|---|---|---|---|
| rabbitmq | `rabbitmq` (Docker) / `localhost` (local) | 5672 | 61613 | guest / guest (dev) |
| rabbitmq (management UI) | localhost | 15672 | — | guest / guest |

Plugin enabled: `rabbitmq_stomp` (via custom image in `rabbitmq-with-stomp-docker/`).

Config: `loopback_users.guest = false` (so `guest` can connect from non-loopback in containers).

---

## Exchanges

| Exchange | Type | Durable | Notes |
|---|---|---|---|
| _(default exchange)_ | direct | — | Used for queue routing by name |

_No custom exchanges yet. If you add one, document it here._

---

## Queues

| Queue | Durable | Auto-delete | Producer | Consumer | Payload | Notes |
|---|---|---|---|---|---|---|
| `after-create-post-queue` | yes | no | `post-service` (`PostServiceImpl.createPost`) | `feed-service` (`PushFeedConsumer`) | `int postId` (gRPC-style integer body) | Triggers feed fan-out to followers |
| `notification-event-queue` | yes | no | _(still missing — see Migration notes below)_ | `notification-service` (`NotificationEventConsumer`) | `NotificationEvent` (JSON or serialized) | Configured in `notification-service/.../MessageQueueConfig.java`; **no producer in this worktree** |
| `notification-event-queue.dlq` | yes | no | _(auto — RabbitMQ DLX)_ | _(none — manual drain only)_ | Same body as the main queue | Dead-letter queue for `notification-event-queue` (Phase 1) |

---

## Producers (per service)

### post-service
- `MessageQueueConfig.AFTER_CREATE_POST_QUEUE` → publishes post ID after `createPost()` commits.

### profile-service
- _(none)_

### feed-service
- _(none — only consumes)_

### notification-service
- _(none — only consumes)_

---

## Consumers (per service)

### post-service
- _(none)_

### profile-service
- _(none)_

### feed-service
- `PushFeedConsumer` listens to `after-create-post-queue`. Calls `post-service` gRPC `getPostsByIds`, then for each post, calls `profile-service` gRPC `getFollowers`, then `LPUSH` postId to each follower's `feed:{profileId}` in Redis.

### notification-service
- `NotificationEventConsumer` listens to `notification-event-queue`. Hydrates the event via `profile-service` gRPC `getProfile`, saves a `Notification` row, then pushes to WebSocket `/topic/notifications/{username}`.
- `MessageQueueConfig` declares the main queue with a dead-letter exchange pointing to the default exchange and routing key `notification-event-queue.dlq`. The DLQ itself is declared as a plain durable queue.

---

## Dead-letter queues

| Queue | Source | Declared by | Purpose |
|---|---|---|---|
| `notification-event-queue.dlq` | `notification-event-queue` (via DLX) | `notification-service` (`MessageQueueConfig.notificationEventDlq`) | Captures poison messages from the notification consumer so a malformed event cannot block the main queue. Add other DLQs as you introduce them. |

---

## Migration notes

- Phase 0: topology documented (this file).
- Phase 1: dead-letter queue `notification-event-queue.dlq` introduced in
  `notification-service` (see `MessageQueueConfig.notificationEventDlq`).
- Phase 1 (open): `notification-event-queue` still has **no producer**.
  `post-service` only publishes to `after-create-post-queue`; the
  notification consumer will receive nothing until a producer is added.
  Tracked follow-up: wire `PostServiceImpl.createPost`,
  `PostServiceImpl.likePost`, and `PostServiceImpl.createComment` in
  `post-service` to publish a `NotificationEvent`; wire
  `ProfileServiceImpl.followUser` in `profile-service` similarly.
- Phase 5: add `after-create-post-queue` producer to `profile-service` for follow events (so followers get feed updates when a followed user creates a post via fan-out).

---

## How to inspect at runtime

```bash
# List queues
docker exec -it <rabbitmq-container> rabbitmqctl list_queues name messages consumers durable

# List exchanges
docker exec -it <rabbitmq-container> rabbitmqctl list_exchanges name type durable

# List bindings
docker exec -it <rabbitmq-container> rabbitmqctl list_bindings

# Purge a queue (dev only)
docker exec -it <rabbitmq-container> rabbitmqctl purge_queue after-create-post-queue

# Drain the notification DLQ (dev only)
docker exec -it <rabbitmq-container> rabbitmqctl purge_queue notification-event-queue.dlq
```

Management UI: <http://localhost:15672> (guest/guest in dev).
