package io.github.eventify.api.notification.adapter.adapters;

import io.github.eventify.api.notification.adapter.client.AdapterClient;
import io.github.eventify.api.notification.adapter.client.AdapterClientFactory;
import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.user.model.User;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Notification adapter for Slack incoming webhooks.
 * Sends Block Kit formatted JSON payloads to a configured Slack webhook URL.
 */
@Component
public class SlackNotificationAdapter implements NotificationAdapter {

    private static final String FIELD_COLOR = "color";
    private static final String FIELD_TITLE = "title";
    private static final String FIELD_TEXT = "text";
    private static final String FIELD_ACTIONS = "actions";
    private static final String FIELD_ATTACHMENTS = "attachments";
    private static final String FIELD_TYPE = "type";
    private static final String FIELD_URL = "url";

    private final AdapterClient adapterClient;

    /** Creates a Slack adapter using the given factory for HTTP client creation. */
    public SlackNotificationAdapter(final AdapterClientFactory factory) {
        this.adapterClient = factory.create("Slack Client");
    }

    @Override
    public void send(final User user, final NotificationPayload payload, final AdapterConfig config) {
        final String webhookUrl = (String) config.getConfig().get("webhookUrl");
        final String color = payload.isUrgent() ? "danger" : "good";

        final Map<String, Object> action = Map.of(
            FIELD_TYPE,
            "button",
            FIELD_TEXT,
            payload.getActionLabel(),
            FIELD_URL,
            payload.getActionUrl()
        );

        final Map<String, Object> attachment = Map.of(
            FIELD_COLOR,
            color,
            FIELD_TITLE,
            payload.getTitle(),
            FIELD_TEXT,
            payload.getMessage(),
            FIELD_ACTIONS,
            List.of(action)
        );

        final Map<String, Object> body = Map.of(FIELD_ATTACHMENTS, List.of(attachment));
        adapterClient.post(webhookUrl, body);
    }

    @Override
    public AdapterType getAdapterType() {
        return AdapterType.SLACK;
    }
}
