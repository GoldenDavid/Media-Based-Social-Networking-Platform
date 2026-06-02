package com.socialnetwork.common.event;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published to the {@code notification-event-queue} when something
 * notification-worthy happens (e.g. a new post by a followed profile).
 *
 * <p>Carried as the message body by RabbitMQ. Serialized with the JDK
 * {@link java.io.Serializable} protocol because both the publisher
 * (post-service) and the consumer (notification-service) load the
 * class from the same {@code socialnetwork-common} JAR, so the
 * {@code serialVersionUID} is stable.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private NotificationType type;
    private int fromProfileId;
    private int toProfileId;
    private int postId;
}
