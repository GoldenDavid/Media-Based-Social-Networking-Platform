package com.socialnetwork.notification.event;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.socialnetwork.notification.config.MessageQueueConfig;
import com.socialnetwork.notification.dto.NotificationDto;
import com.socialnetwork.notification.dto.ProfileDto;
import com.socialnetwork.notification.grpc.ProfileServiceGrpcClient;
import com.socialnetwork.notification.model.Notification;
import com.socialnetwork.notification.repository.NotificationRepository;

import java.util.Date;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@RabbitListener(queues = MessageQueueConfig.NOTIFICATION_EVENT_QUEUE)
public class NotificationEventConsumer {

    private final NotificationRepository notificationRepository;
    private final ProfileServiceGrpcClient profileService;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitHandler
    public void receive(NotificationEvent event) {
        log.info("[x] Received NotificationEvent: {}", event);

        // 1. Save to database
        Notification notification = Notification.builder()
                .fromProfileId(event.getFromProfileId())
                .toProfileId(event.getToProfileId())
                .notificationType(event.getType())
                .postId(event.getPostId())
                .createdAt(new Date())
                .build();
        notification = notificationRepository.save(notification);

        // 2. Fetch profiles via gRPC for rich websocket payload
        ProfileDto fromUser = profileService.getProfile(event.getFromProfileId());
        ProfileDto toUser = profileService.getProfile(event.getToProfileId());

        if (fromUser == null || toUser == null) {
            log.warn("Could not find profile details for notification. from={}, to={}", 
                     event.getFromProfileId(), event.getToProfileId());
            return;
        }

        NotificationDto dto = NotificationDto.builder()
                .id(notification.getId())
                .fromUser(fromUser)
                .toUser(toUser)
                .notificationType(notification.getNotificationType())
                .createdAt(notification.getCreatedAt())
                .postId(notification.getPostId())
                .build();

        // 3. Push to websocket
        String destination = "/topic/notifications/" + toUser.getUsername();
        log.info("Pushing notification to websocket destination: {}", destination);
        messagingTemplate.convertAndSend(destination, dto);
    }
}
