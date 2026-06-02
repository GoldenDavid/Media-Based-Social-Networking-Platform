package com.socialnetwork.notification.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageQueueConfig {
    public static final String NOTIFICATION_EVENT_QUEUE = "notification-event-queue";
    private static final String DLQ = "notification-event-queue.dlq";

    @Bean
    Queue notificationEventQueue() {
        return QueueBuilder.durable(NOTIFICATION_EVENT_QUEUE)
            .deadLetterExchange("")
            .deadLetterRoutingKey(DLQ)
            .build();
    }

    @Bean
    Queue notificationEventDlq() {
        return QueueBuilder.durable(DLQ).build();
    }
}
