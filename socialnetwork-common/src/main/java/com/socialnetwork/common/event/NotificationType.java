package com.socialnetwork.common.event;

/**
 * Categories of in-app notifications.
 *
 * <p>Mirrors the values stored in the {@code notification_type} column of
 * the notification database and surfaced on the websocket
 * {@code /topic/notifications/{username}} destination.
 */
public enum NotificationType {
    NEW_POST,
    LIKE_YOUR_POST,
    COMMENT_YOUR_POST
}
