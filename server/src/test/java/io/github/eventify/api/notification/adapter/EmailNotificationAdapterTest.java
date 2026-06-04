package io.github.eventify.api.notification.adapter;

import io.github.eventify.api.notification.adapter.adapters.EmailNotificationAdapter;
import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.core.model.NotificationCategory;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.email.service.sender.EmailService;
import io.github.eventify.support.UnitTest;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Email Notification Adapter")
public class EmailNotificationAdapterTest extends UnitTest {

    @Mock
    private EmailService emailService;

    private EmailNotificationAdapter adapter;

    @BeforeEach
    public void setUp() {
        adapter = new EmailNotificationAdapter(emailService);
    }

    @Test
    @DisplayName("Should return EMAIL adapter type")
    public void shouldReturnEmailAdapterType() {
        // Given: the adapter instance

        // When: getting the adapter type
        final AdapterType type = adapter.getAdapterType();

        // Then: type is EMAIL
        assertThat(type, is(AdapterType.EMAIL));
    }

    @Test
    @DisplayName("Should call EmailService with rendered notification when user has verified email")
    public void shouldCallEmailServiceWhenUserEmailIsVerified() {
        // Given: a user with a verified email (validated=true)
        final User user = aValidUser();
        user.setValidated(true);
        final NotificationPayload payload = aValidPayload();
        final AdapterConfig config = aValidConfig();

        // When: sending the notification
        adapter.send(user, payload, config);

        // Then: EmailService.sendNotificationEmail is called once
        verify(emailService, times(1)).sendNotificationEmail(eq(user), eq(payload));
    }

    @Test
    @DisplayName("Should skip sending and not call EmailService when user email is not verified")
    public void shouldSkipSendWhenUserEmailIsNotVerified() {
        // Given: a user with an unverified email (validated=false)
        final User user = aValidUser();
        user.setValidated(false);
        final NotificationPayload payload = aValidPayload();
        final AdapterConfig config = aValidConfig();

        // When: sending the notification
        adapter.send(user, payload, config);

        // Then: EmailService is never called
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("Should include notification title in the email payload")
    public void shouldIncludeTitleInEmail() {
        // Given: a user with verified email and a payload with a known title
        final User user = aValidUser();
        user.setValidated(true);
        final NotificationPayload payload = new NotificationPayload(
            NotificationCategory.ALERT,
            "Watchlist Alert: web-errors CRITICAL",
            "Channel moved from WARNING to CRITICAL",
            "/watchlists/10",
            "View Watchlist",
            true,
            null
        );
        final AdapterConfig config = aValidConfig();

        // When: sending
        adapter.send(user, payload, config);

        // Then: the payload passed to EmailService contains the title
        final ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(emailService).sendNotificationEmail(eq(user), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue().getTitle(), containsString("Watchlist Alert: web-errors CRITICAL"));
    }

    @Test
    @DisplayName("Should include notification message in the email payload")
    public void shouldIncludeMessageInEmail() {
        // Given: user with verified email and payload with a known message
        final User user = aValidUser();
        user.setValidated(true);
        final NotificationPayload payload = new NotificationPayload(
            NotificationCategory.ALERT,
            "Alert Title",
            "Channel moved from WARNING to CRITICAL",
            "/watchlists/10",
            "View Watchlist",
            false,
            null
        );
        final AdapterConfig config = aValidConfig();

        // When: sending
        adapter.send(user, payload, config);

        // Then: the payload passed to EmailService contains the notification message
        final ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(emailService).sendNotificationEmail(eq(user), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue().getMessage(), containsString("Channel moved from WARNING to CRITICAL"));
    }

    @Test
    @DisplayName("Should delegate to sendNotificationEmail (HTML rendering handled by EmailService)")
    public void shouldSendHtmlEmail() {
        // Given: user with verified email
        final User user = aValidUser();
        user.setValidated(true);
        final NotificationPayload payload = aValidPayload();
        final AdapterConfig config = aValidConfig();

        // When: sending
        adapter.send(user, payload, config);

        // Then: sendNotificationEmail is called (template rendering is EmailService's responsibility)
        verify(emailService).sendNotificationEmail(eq(user), eq(payload));
    }

    // ========================= FACTORY METHODS =========================

    private static NotificationPayload aValidPayload() {
        return new NotificationPayload(
            NotificationCategory.ANNOUNCEMENT,
            "Test Notification",
            "This is a test notification message",
            "/watchlists/1",
            "View Watchlist",
            false,
            null
        );
    }

    private static AdapterConfig aValidConfig() {
        final AdapterConfig config = new AdapterConfig();
        config.setAdapterType(AdapterType.EMAIL);
        config.setLabel("My Email Adapter");
        config.setConfig(Map.of());
        config.setEnabled(true);
        return config;
    }
}
