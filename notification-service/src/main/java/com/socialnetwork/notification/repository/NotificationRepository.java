package com.socialnetwork.notification.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.socialnetwork.notification.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    /**
     * Returns the notification history for {@code toProfileId} sorted by
     * {@code createdAt} descending (most recent first), paginated.
     *
     * <p>Spring Data derives the implementation from the method name; the
     * default page size and sort live on the {@link Pageable} parameter
     * (caller-supplied).
     */
    Page<Notification> findByToProfileIdOrderByCreatedAtDesc(int toProfileId, Pageable pageable);

    /**
     * Kept for backward compatibility with callers that don't need pagination.
     */
    List<Notification> findByToProfileId(int toProfileId);
}
