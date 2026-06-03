package io.github.eventify.api.notification.adapter;

import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.user.model.User;

/**
 * No-op notification adapter — for testing or as a fallback. Does nothing on send.
 */
public class NoOpNotificationAdapter implements NotificationAdapter {

    private final AdapterType adapterType;

    /**
     * Creates a no-op adapter with the given type identifier.
     *
     * @param adapterType the adapter type this no-op will report
     */
    public NoOpNotificationAdapter(final AdapterType adapterType) {
        this.adapterType = adapterType;
    }

    @Override
    public AdapterType getAdapterType() {
        return adapterType;
    }

    @Override
    public void send(final User user, final NotificationPayload payload) {
        // no-op
    }
}
