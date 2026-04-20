package com.socialnetwork.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.socialnetwork.event.PushFeedConsumer;

@Configuration
public class MessageQueueConfig {
  public static final String AFTER_CREATE_POST_QUEUE = "after-create-post-queue";

  @Bean
  Queue afterCreatePostQueue() {
    return QueueBuilder.durable(AFTER_CREATE_POST_QUEUE).build();

  }

  @Bean
  PushFeedConsumer initConsumer() {
    return new PushFeedConsumer();
  }
}
