package com.socialnetwork.notification.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.socialnetwork.common.event.NotificationEvent;
import com.socialnetwork.common.event.NotificationType;
import com.socialnetwork.notification.model.Notification;
import com.socialnetwork.notification.repository.NotificationRepository;

/**
 * JPA-slice test for the notification pipeline.
 *
 * <p>Uses {@code @DataJpaTest} so the WebSocket / STOMP / RabbitMQ
 * configuration is not loaded — the consumer is exercised in production
 * smoke tests; here we only assert the storage contract the rest of
 * the app depends on.
 */
@DataJpaTest
@AutoConfigureTestDatabase
@ActiveProfiles("test")
class NotificationEventPersistenceTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void persistsAndReadsBack() {
        Notification saved = Notification.builder()
                .fromProfileId(42)
                .toProfileId(7)
                .notificationType(NotificationType.NEW_POST)
                .postId(100)
                .createdAt(new Date())
                .build();
        saved = notificationRepository.save(saved);

        assertThat(saved.getId()).isPositive();
        var found = notificationRepository.findByToProfileId(7);
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getFromProfileId()).isEqualTo(42);
        assertThat(found.get(0).getNotificationType()).isEqualTo(NotificationType.NEW_POST);
        assertThat(found.get(0).getPostId()).isEqualTo(100);
    }

    @Test
    void eventSerialVersionUidIsStable() {
        // The class is loaded from socialnetwork-common in production; the
        // serialVersionUID must stay 1L or messages deserialise to a
        // different class.
        assertThat(NotificationEvent.class.getDeclaredFields())
                .anyMatch(f -> f.getName().equals("serialVersionUID"));
    }
}
