package io.github.eventify.api.notification.adapter;

import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.user.model.User;

/**
 * Adapter interface for sending notifications via different channels.
 */
public interface NotificationAdapter {

    /**
     * Returns the type of this adapter.
     *
     * @return the adapter type
     */
    AdapterType getAdapterType();

    /**
     * Sends a notification to a user.
     *
     * @param user    the recipient user
     * @param payload the notification payload
     */
    void send(User user, NotificationPayload payload);
}
