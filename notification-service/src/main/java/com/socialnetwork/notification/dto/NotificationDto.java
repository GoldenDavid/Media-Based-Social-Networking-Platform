package com.socialnetwork.notification.dto;

import com.socialnetwork.notification.model.NotificationType;
import lombok.Builder;
import lombok.Data;
import java.util.Date;

@Data
@Builder
public class NotificationDto {
    private int id;
    private ProfileDto fromUser;
    private ProfileDto toUser;
    private NotificationType notificationType;
    private Date createdAt;
    private int postId;
}
