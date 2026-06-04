package io.github.eventify.api.notification.adapter;

import io.github.eventify.api.notification.adapter.adapters.SlackNotificationAdapter;
import io.github.eventify.api.notification.adapter.client.AdapterClient;
import io.github.eventify.api.notification.adapter.client.AdapterClientFactory;
import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.core.model.NotificationCategory;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.UnitTest;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Slack Notification Adapter")
public class SlackNotificationAdapterTest extends UnitTest {

    @Mock
    private AdapterClient adapterClient;

    @Mock
    private AdapterClientFactory factory;

    private SlackNotificationAdapter adapter;

    @BeforeEach
    public void setUp() {
        when(factory.create(anyString())).thenReturn(adapterClient);
        adapter = new SlackNotificationAdapter(factory);
    }

    @Test
    @DisplayName("Should return SLACK adapter type")
    public void shouldReturnSlackAdapterType() {
        final AdapterType type = adapter.getAdapterType();
        assertThat(type, is(AdapterType.SLACK));
    }

    @Test
    @DisplayName("Should POST message to webhook URL from AdapterConfig")
    public void shouldPostToWebhookUrlFromConfig() {
        final User user = aValidUser();
        final NotificationPayload payload = aValidPayload();
        final AdapterConfig config = aConfigWithWebhookUrl("https://hooks.slack.com/services/T00/B00/xxx");

        adapter.send(user, payload, config);

        verify(adapterClient, times(1)).post(eq("https://hooks.slack.com/services/T00/B00/xxx"), any());
    }

    @Test
    @DisplayName("Should include title in the Block Kit message body")
    public void shouldIncludeTitleInMessage() {
        final User user = aValidUser();
        final NotificationPayload payload = new NotificationPayload(
            NotificationCategory.ALERT,
            "Production Alert",
            "CPU threshold exceeded on channel web-errors",
            "/watchlists/42",
            "View Watchlist",
            true,
            null
        );
        final AdapterConfig config = aConfigWithWebhookUrl("https://hooks.slack.com/services/T00/B00/xxx");

        adapter.send(user, payload, config);

        final ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(adapterClient).post(anyString(), bodyCaptor.capture());
        final String body = bodyCaptor.getValue().toString();
        assertThat(body, containsString("Production Alert"));
    }

    @Test
    @DisplayName("Should color attachment danger for urgent payload")
    public void shouldColorAttachmentDangerForUrgent() {
        final User user = aValidUser();
        final NotificationPayload payload = new NotificationPayload(
            NotificationCategory.ALERT,
            "Critical Alert",
            "Disk full",
            "/channels",
            "View",
            true,
            null
        );
        final AdapterConfig config = aConfigWithWebhookUrl("https://hooks.slack.com/services/T00/B00/xxx");

        adapter.send(user, payload, config);

        final ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(adapterClient).post(anyString(), bodyCaptor.capture());
        final String body = bodyCaptor.getValue().toString();
        assertThat(body, containsString("danger"));
    }

    @Test
    @DisplayName("Should color attachment good for non-urgent payload")
    public void shouldColorAttachmentGoodForNonUrgent() {
        final User user = aValidUser();
        final NotificationPayload payload = new NotificationPayload(
            NotificationCategory.ALERT,
            "Info Alert",
            "All good",
            "/dashboard",
            "View",
            false,
            null
        );
        final AdapterConfig config = aConfigWithWebhookUrl("https://hooks.slack.com/services/T00/B00/xxx");

        adapter.send(user, payload, config);

        final ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(adapterClient).post(anyString(), bodyCaptor.capture());
        final String body = bodyCaptor.getValue().toString();
        assertThat(body, containsString("good"));
    }

    // ========================= FACTORY METHODS =========================

    private static NotificationPayload aValidPayload() {
        return new NotificationPayload(
            NotificationCategory.ANNOUNCEMENT,
            "Test Alert",
            "Something needs attention",
            "/watchlists/1",
            "View Watchlist",
            false,
            null
        );
    }

    private static AdapterConfig aConfigWithWebhookUrl(final String webhookUrl) {
        final AdapterConfig config = new AdapterConfig();
        config.setAdapterType(AdapterType.SLACK);
        config.setLabel("My Slack");
        config.setConfig(Map.of("webhookUrl", webhookUrl));
        config.setEnabled(true);
        return config;
    }
}
