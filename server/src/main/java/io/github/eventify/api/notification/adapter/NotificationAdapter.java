package io.github.eventify.api.notification.adapter;

import io.github.eventify.api.notification.model.NotificationPayload;
import io.github.eventify.api.user.model.User;

/**
 * Adapter interface for sending notifications via different channels.
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface NotificationAdapter {

    /**
     * Sends a notification to a user.
     *
     * @param user    the recipient user
     * @param payload the notification payload
     */
    void send(User user, NotificationPayload payload);
}
