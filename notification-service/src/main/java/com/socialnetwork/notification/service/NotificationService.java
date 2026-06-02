package com.socialnetwork.notification.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.notification.dto.NotificationDto;
import com.socialnetwork.notification.dto.NotificationsResponse;
import com.socialnetwork.notification.dto.ProfileDto;
import com.socialnetwork.notification.grpc.ProfileServiceGrpcClient;
import com.socialnetwork.notification.model.Notification;
import com.socialnetwork.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Read-side service for notification history.
 *
 * <p>Mutations are handled by {@link com.socialnetwork.notification.event.NotificationEventConsumer}
 * (RabbitMQ-driven); this class only serves HTTP GETs from the
 * frontend notification drawer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProfileServiceGrpcClient profileService;

    public NotificationsResponse getMyNotifications(UserPrincipal principal, int page, int limit) {
        int profileId = resolveProfileId(principal);

        var pageable = PageRequest.of(page - 1, limit);
        Page<Notification> result = notificationRepository.findByToProfileIdOrderByCreatedAtDesc(profileId, pageable);

        List<NotificationDto> dtos = result.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        int totalCount = (int) result.getTotalElements();
        int totalPage = result.getTotalPages();

        log.debug("getMyNotifications profileId={} page={} limit={} -> totalCount={} totalPage={}",
                profileId, page, limit, totalCount, totalPage);

        return NotificationsResponse.builder()
                .totalPage(totalPage)
                .totalCount(totalCount)
                .notifications(dtos)
                .build();
    }

    /**
     * Resolves the numeric profileId for a session principal by calling the
     * profile-service gRPC {@code getOrCreateProfileByUserId}. The result is
     * the same identifier that the post-service producer uses when
     * publishing {@code NotificationEvent.toProfileId}, so a single query
     * path is sufficient.
     */
    private int resolveProfileId(UserPrincipal principal) {
        return profileService.getProfile(principal).getId();
    }

    private NotificationDto toDto(Notification n) {
        ProfileDto from = safeProfile(n.getFromProfileId());
        ProfileDto to = safeProfile(n.getToProfileId());
        return NotificationDto.builder()
                .id(n.getId())
                .fromUser(from)
                .toUser(to)
                .notificationType(n.getNotificationType())
                .createdAt(n.getCreatedAt())
                .postId(n.getPostId())
                .build();
    }

    private ProfileDto safeProfile(int profileId) {
        try {
            return profileService.getProfile(profileId);
        } catch (Exception ex) {
            log.warn("Failed to load profileId={} for notification DTO: {}", profileId, ex.getMessage());
            return null;
        }
    }
}
