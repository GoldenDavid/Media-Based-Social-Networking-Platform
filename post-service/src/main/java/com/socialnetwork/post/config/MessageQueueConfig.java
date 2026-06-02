package com.socialnetwork.post.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageQueueConfig {
  public static final String AFTER_CREATE_POST_QUEUE = "after-create-post-queue";
  private static final String DLQ = "after-create-post-queue.dlq";

  /**
   * Routing key for the notification fan-out. The queue itself is declared
   * and consumed by the notification-service; we only publish to it.
   */
  public static final String NOTIFICATION_EVENT_QUEUE = "notification-event-queue";

  @Bean
  Queue afterCreatePostQueue() {
    return QueueBuilder.durable(AFTER_CREATE_POST_QUEUE)
        .deadLetterExchange("")
        .deadLetterRoutingKey(DLQ)
        .build();
  }

  @Bean
  Queue afterCreatePostDlq() {
    return QueueBuilder.durable(DLQ).build();
  }
}
