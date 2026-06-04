package io.github.eventify.api.notification.adapter.adapters;

import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.user.model.User;

/**
 * Adapter interface for sending notifications via different channels.
 */
public interface NotificationAdapter {

    /**
     * Sends a notification to a user with adapter-specific configuration context. Adapters that require configuration (webhook URL,
     * credentials, etc.) must override this method.
     *
     * @param user    the recipient user
     * @param payload the notification payload
     * @param config  the adapter configuration providing runtime parameters (e.g. webhookUrl)
     */
    void send(User user, NotificationPayload payload, AdapterConfig config);

    /**
     * Returns the type of this adapter.
     *
     * @return the adapter type
     */
    AdapterType getAdapterType();

}
