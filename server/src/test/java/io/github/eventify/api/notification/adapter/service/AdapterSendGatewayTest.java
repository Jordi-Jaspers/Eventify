package io.github.eventify.api.notification.adapter.service;

import io.github.eventify.api.notification.adapter.adapters.NotificationAdapter;
import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.core.model.NotificationCategory;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Adapter Send Gateway")
public class AdapterSendGatewayTest extends UnitTest {

    @Mock
    private NotificationAdapter adapter;

    private AdapterSendGateway gateway;

    @BeforeEach
    public void setUp() {
        gateway = new AdapterSendGateway();
    }

    @Test
    @DisplayName("Should delegate to adapter.send() with given arguments")
    public void shouldDelegateToAdapterSend() {
        final User user = aValidUser();
        final NotificationPayload payload = aValidPayload();
        final AdapterConfig config = null;

        gateway.sendAsync(adapter, user, payload, config);

        verify(adapter, times(1)).send(user, payload, config);
    }

    @Test
    @DisplayName("Should not rethrow when adapter.send() throws")
    public void shouldCatchAdapterException() {
        final User user = aValidUser();
        final NotificationPayload payload = aValidPayload();

        doThrow(new RuntimeException("Webhook failed")).when(adapter).send(any(), any(), any());

        gateway.sendAsync(adapter, user, payload, null);
    }

    @Test
    @DisplayName("Should continue to next call when adapter.send() throws (failure isolation)")
    public void shouldContinueWhenAdapterThrows() {
        final User user = aValidUser();
        final NotificationPayload payload = aValidPayload();

        doThrow(new RuntimeException("First call failed"))
            .doNothing()
            .when(adapter).send(any(), any(), any());

        gateway.sendAsync(adapter, user, payload, null);
        gateway.sendAsync(adapter, user, payload, null);

        verify(adapter, times(2)).send(any(), any(), any());
    }

    // ========================= FACTORY METHODS =========================

    private static NotificationPayload aValidPayload() {
        return new NotificationPayload(
            NotificationCategory.ANNOUNCEMENT,
            "Test",
            "Test message",
            "/test",
            "View",
            false,
            null
        );
    }
}
