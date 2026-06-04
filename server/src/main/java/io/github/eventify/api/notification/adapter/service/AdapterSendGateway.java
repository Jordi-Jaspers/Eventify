package io.github.eventify.api.notification.adapter.service;

import io.github.eventify.api.notification.adapter.adapters.NotificationAdapter;
import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.user.model.User;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Async gateway for firing adapter send calls off the calling thread.
 */
@Slf4j
@Component
public class AdapterSendGateway {

    /**
     * Asynchronously sends a notification via the given adapter using a resolved config.
     */
    @Async
    public void sendAsync(final NotificationAdapter adapter, final User user, final NotificationPayload payload,
        final AdapterConfig config) {
        try {
            adapter.send(user, payload, config);
        } catch (final Exception e) {
            log.warn("Adapter '{}' failed for user '{}': '{}'", adapter.getAdapterType(), user.getId(), e.getMessage());
        }
    }
}
