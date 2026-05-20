package io.github.eventify.api.notification.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Immutable payload for dispatching a notification.
 */
@Getter
@RequiredArgsConstructor
public class NotificationPayload {

    private final NotificationCategory category;
    private final String title;
    private final String message;
    private final String actionUrl;
    private final String actionLabel;
    private final boolean urgent;
    private final NotificationBroadcast broadcast;

}
