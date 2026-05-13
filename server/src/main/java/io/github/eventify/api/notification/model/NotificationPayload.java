package io.github.eventify.api.notification.model;

import lombok.Getter;

/**
 * Immutable payload for dispatching a notification.
 */
@Getter
public class NotificationPayload {

    private final NotificationCategory category;
    private final String title;
    private final String message;
    private final String actionUrl;
    private final String actionLabel;
    private final boolean urgent;
    private final NotificationBroadcast broadcast;

    /**
     * Creates a payload without a broadcast reference.
     *
     * @param category    the notification category
     * @param title       the notification title
     * @param message     the notification message
     * @param actionUrl   optional action URL
     * @param actionLabel optional action label
     * @param urgent      whether the notification is urgent
     */
    public NotificationPayload(final NotificationCategory category,
                               final String title,
                               final String message,
                               final String actionUrl,
                               final String actionLabel,
                               final boolean urgent) {
        this(category, title, message, actionUrl, actionLabel, urgent, null);
    }

    /**
     * Creates a payload with a broadcast reference.
     *
     * @param category    the notification category
     * @param title       the notification title
     * @param message     the notification message
     * @param actionUrl   optional action URL
     * @param actionLabel optional action label
     * @param urgent      whether the notification is urgent
     * @param broadcast   the associated broadcast, or null
     */
    public NotificationPayload(final NotificationCategory category,
                               final String title,
                               final String message,
                               final String actionUrl,
                               final String actionLabel,
                               final boolean urgent,
                               final NotificationBroadcast broadcast) {
        this.category = category;
        this.title = title;
        this.message = message;
        this.actionUrl = actionUrl;
        this.actionLabel = actionLabel;
        this.urgent = urgent;
        this.broadcast = broadcast;
    }
}
