package io.github.eventify.api.notification.adapter.service;

import io.github.eventify.api.notification.adapter.AdapterRegistry;
import io.github.eventify.api.notification.adapter.adapters.NotificationAdapter;
import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.adapter.model.response.TestConnectionResponse;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.security.SecurityUtil;
import io.github.eventify.support.UnitTest;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Adapter Test Service")
public class AdapterTestServiceTest extends UnitTest {

    @Mock
    private AdapterRegistry adapterRegistry;

    @Mock
    private NotificationAdapter adapter;

    private AdapterTestService adapterTestService;

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    public void setUp() {
        adapterTestService = new AdapterTestService(adapterRegistry);
        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getLoggedInUser).thenReturn(aValidUser());
    }

    @AfterEach
    public void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    @Test
    @DisplayName("Should return success=true when adapter sends successfully")
    public void shouldReturnSuccessTrueWhenAdapterSendsSuccessfully() {
        // Given: a working adapter
        when(adapterRegistry.getByAdapterType(AdapterType.MATTERMOST)).thenReturn(Optional.of(adapter));
        doNothing().when(adapter).send(any(User.class), any(NotificationPayload.class), any(AdapterConfig.class));

        // When: testing the connection
        final TestConnectionResponse response = adapterTestService.testConnection(AdapterType.MATTERMOST, "https://example.com/webhook");

        // Then: success is true and no error
        assertThat(response.isSuccess(), is(true));
        assertThat(response.getError(), is(nullValue()));
    }

    @Test
    @DisplayName("Should return success=false with error message when adapter throws")
    public void shouldReturnSuccessFalseWhenAdapterThrows() {
        // Given: an adapter that throws
        when(adapterRegistry.getByAdapterType(AdapterType.SLACK)).thenReturn(Optional.of(adapter));
        doThrow(new RuntimeException("Connection refused")).when(adapter)
            .send(any(User.class), any(NotificationPayload.class), any(AdapterConfig.class));

        // When: testing the connection
        final TestConnectionResponse response = adapterTestService.testConnection(
            AdapterType.SLACK,
            "https://hooks.slack.com/services/INVALID"
        );

        // Then: success is false, error message is propagated
        assertThat(response.isSuccess(), is(false));
        assertThat(response.getError(), is(notNullValue()));
        assertThat(response.getError(), containsString("Connection refused"));
    }

    @Test
    @DisplayName("Should not propagate exception when adapter fails — failure is returned in response")
    public void shouldNotPropagateExceptionWhenAdapterFails() {
        // Given: an adapter that throws a runtime exception
        when(adapterRegistry.getByAdapterType(AdapterType.MATTERMOST)).thenReturn(Optional.of(adapter));
        doThrow(new RuntimeException("Webhook unreachable")).when(adapter)
            .send(any(User.class), any(NotificationPayload.class), any(AdapterConfig.class));

        // When / Then: no exception thrown
        final TestConnectionResponse response = adapterTestService.testConnection(AdapterType.MATTERMOST, "https://example.com/webhook");
        assertThat(response, is(notNullValue()));
        assertThat(response.isSuccess(), is(false));
    }

    @Test
    @DisplayName("Should send a canned test payload to the adapter")
    public void shouldSendCannedTestPayloadToAdapter() {
        // Given: a working adapter
        when(adapterRegistry.getByAdapterType(AdapterType.EMAIL)).thenReturn(Optional.of(adapter));

        // When: testing
        adapterTestService.testConnection(AdapterType.EMAIL, "https://example.com/webhook");

        // Then: adapter.send is called with a non-null payload
        final ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(adapter).send(any(User.class), payloadCaptor.capture(), any(AdapterConfig.class));
        assertThat(payloadCaptor.getValue(), is(notNullValue()));
        assertThat(payloadCaptor.getValue().getTitle(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should pass a transient config with the given webhook URL to the adapter")
    public void shouldPassTransientConfigWithWebhookUrlToAdapter() {
        // Given: a working adapter
        when(adapterRegistry.getByAdapterType(AdapterType.MATTERMOST)).thenReturn(Optional.of(adapter));
        final String webhookUrl = "https://example.com/hook";

        // When: testing connection
        adapterTestService.testConnection(AdapterType.MATTERMOST, webhookUrl);

        // Then: config passed to adapter contains the webhook URL
        final ArgumentCaptor<AdapterConfig> configCaptor = ArgumentCaptor.forClass(AdapterConfig.class);
        verify(adapter).send(any(User.class), any(NotificationPayload.class), configCaptor.capture());
        assertThat(configCaptor.getValue().getConfig().get("webhookUrl"), is(webhookUrl));
    }

    @Test
    @DisplayName("Should return failure when no adapter is registered for the given type")
    public void shouldReturnFailureWhenNoAdapterRegistered() {
        // Given: no adapter registered
        when(adapterRegistry.getByAdapterType(AdapterType.SLACK)).thenReturn(Optional.empty());

        // When: testing connection
        final TestConnectionResponse response = adapterTestService.testConnection(AdapterType.SLACK, "https://example.com/hook");

        // Then: failure is returned
        assertThat(response.isSuccess(), is(false));
        assertThat(response.getError(), is(notNullValue()));
        verifyNoMoreInteractions(adapter);
    }
}
