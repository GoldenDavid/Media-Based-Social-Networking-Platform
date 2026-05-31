package com.socialnetwork.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.socialnetwork.notification.model.NotificationType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private NotificationType type;
    private int fromProfileId;
    private int toProfileId;
    private int postId;
}
