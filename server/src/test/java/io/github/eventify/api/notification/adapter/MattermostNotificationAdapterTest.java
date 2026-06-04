package io.github.eventify.api.notification.adapter;

import io.github.eventify.api.notification.adapter.adapters.MattermostNotificationAdapter;
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

@DisplayName("Unit Test - Mattermost Notification Adapter")
public class MattermostNotificationAdapterTest extends UnitTest {

    @Mock
    private AdapterClient adapterClient;

    @Mock
    private AdapterClientFactory factory;

    private MattermostNotificationAdapter adapter;

    @BeforeEach
    public void setUp() {
        when(factory.create(anyString())).thenReturn(adapterClient);
        adapter = new MattermostNotificationAdapter(factory);
    }

    @Test
    @DisplayName("Should return MATTERMOST adapter type")
    public void shouldReturnMattermostAdapterType() {
        final AdapterType type = adapter.getAdapterType();
        assertThat(type, is(AdapterType.MATTERMOST));
    }

    @Test
    @DisplayName("Should POST message to webhook URL from AdapterConfig")
    public void shouldPostToWebhookUrlFromConfig() {
        final User user = aValidUser();
        final NotificationPayload payload = aValidPayload();
        final AdapterConfig config = aConfigWithWebhookUrl("https://mattermost.example.com/hooks/abc123");

        adapter.send(user, payload, config);

        verify(adapterClient, times(1)).post(eq("https://mattermost.example.com/hooks/abc123"), any());
    }

    @Test
    @DisplayName("Should include title in the markdown message body")
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
        final AdapterConfig config = aConfigWithWebhookUrl("https://mattermost.example.com/hooks/xyz");

        adapter.send(user, payload, config);

        final ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(adapterClient).post(anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue().toString(), containsString("Production Alert"));
    }

    @Test
    @DisplayName("Should include message body in the markdown payload")
    public void shouldIncludeMessageBodyInPayload() {
        final User user = aValidUser();
        final NotificationPayload payload = new NotificationPayload(
            NotificationCategory.ALERT,
            "My Title",
            "CPU threshold exceeded on channel web-errors",
            "/watchlists/42",
            "View Watchlist",
            false,
            null
        );
        final AdapterConfig config = aConfigWithWebhookUrl("https://mattermost.example.com/hooks/xyz");

        adapter.send(user, payload, config);

        final ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(adapterClient).post(anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue().toString(), containsString("CPU threshold exceeded on channel web-errors"));
    }

    @Test
    @DisplayName("Should include action URL and label in markdown message")
    public void shouldIncludeActionLinkInMessage() {
        final User user = aValidUser();
        final NotificationPayload payload = new NotificationPayload(
            NotificationCategory.ALERT,
            "Alert",
            "Something happened",
            "/watchlists/99",
            "View Watchlist",
            false,
            null
        );
        final AdapterConfig config = aConfigWithWebhookUrl("https://mattermost.example.com/hooks/xyz");

        adapter.send(user, payload, config);

        final ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(adapterClient).post(anyString(), bodyCaptor.capture());
        final String body = bodyCaptor.getValue().toString();
        assertThat(body, containsString("/watchlists/99"));
        assertThat(body, containsString("View Watchlist"));
    }

    @Test
    @DisplayName("Should include severity icon for urgent payload")
    public void shouldIncludeUrgentIconForUrgentPayload() {
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
        final AdapterConfig config = aConfigWithWebhookUrl("https://mattermost.example.com/hooks/xyz");

        adapter.send(user, payload, config);

        final ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
        verify(adapterClient).post(anyString(), bodyCaptor.capture());
        final String body = bodyCaptor.getValue().toString();
        assertThat(
            body,
            anyOf(
                containsString("\uD83D\uDD34"),
                containsString("\uD83D\uDEA8"),
                containsString(":red_circle:"),
                containsString(":rotating_light:"),
                containsString("urgent"),
                containsString("URGENT"),
                containsString("critical"),
                containsString("CRITICAL")
            )
        );
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
        config.setAdapterType(AdapterType.MATTERMOST);
        config.setLabel("My Mattermost");
        config.setConfig(Map.of("webhookUrl", webhookUrl));
        config.setEnabled(true);
        return config;
    }
}
