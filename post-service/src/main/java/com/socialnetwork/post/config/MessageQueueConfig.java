package com.socialnetwork.post.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageQueueConfig {
  public static final String AFTER_CREATE_POST_QUEUE = "after-create-post-queue";
  private static final String DLQ = "after-create-post-queue.dlq";

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
