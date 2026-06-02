package com.socialnetwork.notification.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Paginated notification history, returned by
 * {@code GET /notifications/me} wrapped in {@code BaseResponse}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationsResponse {
    private int totalPage;
    private int totalCount;
    private List<NotificationDto> notifications;
}
