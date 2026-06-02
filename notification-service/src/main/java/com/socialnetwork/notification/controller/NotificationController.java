package com.socialnetwork.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.socialnetwork.common.dto.BaseResponse;
import com.socialnetwork.common.security.UserPrincipal;
import com.socialnetwork.notification.dto.NotificationsResponse;
import com.socialnetwork.notification.service.NotificationService;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST endpoints for notification reads.
 *
 * <p>Mutations are driven by the {@code notification-event-queue} consumer
 * (see {@link com.socialnetwork.notification.event.NotificationEventConsumer});
 * this controller is read-only.
 */
@Slf4j
@RestController
@RequestMapping("/notifications")
@Validated
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Returns the notification history for the authenticated user, most
     * recent first. The backend identifies the user via the session's
     * {@link UserPrincipal}; the {@code profileId} is read from there so
     * the caller cannot spoof another user's history.
     */
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<NotificationsResponse>> getMyNotifications(
            Authentication authentication,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "limit", defaultValue = "20") @Min(1) int limit) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        log.info("getMyNotifications userId={} page={} limit={}", principal.getId(), page, limit);
        NotificationsResponse body = notificationService.getMyNotifications(principal, page, limit);
        return ResponseEntity.ok(BaseResponse.ok(body));
    }
}
