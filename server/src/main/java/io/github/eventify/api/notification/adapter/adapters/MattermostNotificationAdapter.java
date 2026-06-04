package io.github.eventify.api.notification.adapter.adapters;

import io.github.eventify.api.notification.adapter.client.AdapterClient;
import io.github.eventify.api.notification.adapter.client.AdapterClientFactory;
import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.user.model.User;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Notification adapter for Mattermost webhooks.
 * Sends markdown-formatted messages to a configured Mattermost incoming webhook URL.
 */
@Slf4j
@Component
public class MattermostNotificationAdapter implements NotificationAdapter {

    private final AdapterClient adapterClient;

    /** Creates a Mattermost adapter using the given factory for HTTP client creation. */
    public MattermostNotificationAdapter(final AdapterClientFactory factory) {
        this.adapterClient = factory.create("Mattermost Client");
    }

    @Override
    public void send(final User user, final NotificationPayload payload, final AdapterConfig config) {
        log.debug("Mattermost adapter sending notification to user '{}': {}", user.getId(), payload.getTitle());
        final String webhookUrl = (String) config.getConfig().get("webhookUrl");
        final String icon = payload.isUrgent() ? "\uD83D\uDD34" : "\uD83D\uDFE2";
        final String text = icon + " **" + payload.getTitle() + "**\n\n"
            + payload.getMessage() + "\n\n"
            + "[" + payload.getActionLabel() + "](" + payload.getActionUrl() + ")";
        final Map<String, String> body = Map.of("text", text);
        adapterClient.post(webhookUrl, body);
    }

    @Override
    public AdapterType getAdapterType() {
        return AdapterType.MATTERMOST;
    }
}
