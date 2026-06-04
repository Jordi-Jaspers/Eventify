package io.github.eventify.api.notification.adapter.service;

import io.github.eventify.api.notification.adapter.AdapterRegistry;
import io.github.eventify.api.notification.adapter.adapters.NotificationAdapter;
import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.adapter.model.response.TestConnectionResponse;
import io.github.eventify.api.notification.adapter.repository.AdapterConfigRepository;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.UnitTest;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Adapter Test Service")
public class AdapterTestServiceTest extends UnitTest {

    @Mock
    private AdapterConfigRepository adapterConfigRepository;

    @Mock
    private AdapterRegistry adapterRegistry;

    @Mock
    private NotificationAdapter adapter;

    private AdapterTestService adapterTestService;

    @BeforeEach
    public void setUp() {
        adapterTestService = new AdapterTestService(adapterConfigRepository, adapterRegistry);
    }

    @Test
    @DisplayName("Should return success=true when adapter sends successfully")
    public void shouldReturnSuccessTrueWhenAdapterSendsSuccessfully() {
        // Given: a config owned by the user and a working adapter
        final User user = aValidUser();
        final AdapterConfig config = aConfigForUser(user, AdapterType.MATTERMOST);

        when(adapterConfigRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.of(config));
        when(adapterRegistry.getByAdapterType(AdapterType.MATTERMOST)).thenReturn(Optional.of(adapter));
        doNothing().when(adapter).send(any(User.class), any(NotificationPayload.class), any(AdapterConfig.class));

        // When: testing the connection
        final TestConnectionResponse response = adapterTestService.testConnection(1L, user.getId());

        // Then: success is true and no error
        assertThat(response.isSuccess(), is(true));
        assertThat(response.getError(), is(nullValue()));
    }

    @Test
    @DisplayName("Should return success=false with error message when adapter throws")
    public void shouldReturnSuccessFalseWhenAdapterThrows() {
        // Given: a config owned by the user and an adapter that throws
        final User user = aValidUser();
        final AdapterConfig config = aConfigForUser(user, AdapterType.SLACK);

        when(adapterConfigRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.of(config));
        when(adapterRegistry.getByAdapterType(AdapterType.SLACK)).thenReturn(Optional.of(adapter));
        doThrow(new RuntimeException("Connection refused")).when(adapter)
            .send(any(User.class), any(NotificationPayload.class), any(AdapterConfig.class));

        // When: testing the connection
        final TestConnectionResponse response = adapterTestService.testConnection(1L, user.getId());

        // Then: success is false, error message is propagated
        assertThat(response.isSuccess(), is(false));
        assertThat(response.getError(), is(notNullValue()));
        assertThat(response.getError(), containsString("Connection refused"));
    }

    @Test
    @DisplayName("Should not throw exception when adapter fails — failure is returned in response")
    public void shouldNotPropagateExceptionWhenAdapterFails() {
        // Given: an adapter that throws a runtime exception
        final User user = aValidUser();
        final AdapterConfig config = aConfigForUser(user, AdapterType.MATTERMOST);

        when(adapterConfigRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.of(config));
        when(adapterRegistry.getByAdapterType(AdapterType.MATTERMOST)).thenReturn(Optional.of(adapter));
        doThrow(new RuntimeException("Webhook unreachable")).when(adapter)
            .send(any(User.class), any(NotificationPayload.class), any(AdapterConfig.class));

        // When / Then: no exception thrown
        final TestConnectionResponse response = adapterTestService.testConnection(1L, user.getId());
        assertThat(response, is(notNullValue()));
        assertThat(response.isSuccess(), is(false));
    }

    @Test
    @DisplayName("Should send a canned test payload to the adapter")
    public void shouldSendCannedTestPayloadToAdapter() {
        // Given: a working adapter and config
        final User user = aValidUser();
        final AdapterConfig config = aConfigForUser(user, AdapterType.EMAIL);

        when(adapterConfigRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.of(config));
        when(adapterRegistry.getByAdapterType(AdapterType.EMAIL)).thenReturn(Optional.of(adapter));

        // When: testing
        adapterTestService.testConnection(1L, user.getId());

        // Then: adapter.send is called with a non-null payload
        final ArgumentCaptor<NotificationPayload> payloadCaptor = ArgumentCaptor.forClass(NotificationPayload.class);
        verify(adapter).send(any(User.class), payloadCaptor.capture(), eq(config));
        assertThat(payloadCaptor.getValue(), is(notNullValue()));
        assertThat(payloadCaptor.getValue().getTitle(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should use AdapterConfig from the resolved config when calling adapter")
    public void shouldPassResolvedConfigToAdapter() {
        // Given: a config for the user
        final User user = aValidUser();
        final AdapterConfig config = aConfigForUser(user, AdapterType.MATTERMOST);

        when(adapterConfigRepository.findByIdAndUserId(1L, user.getId())).thenReturn(Optional.of(config));
        when(adapterRegistry.getByAdapterType(AdapterType.MATTERMOST)).thenReturn(Optional.of(adapter));

        // When: testing connection
        adapterTestService.testConnection(1L, user.getId());

        // Then: the exact same config is passed to the adapter
        verify(adapter).send(any(User.class), any(NotificationPayload.class), eq(config));
    }

    @Test
    @DisplayName("Should return success=false with error when config not found for user")
    public void shouldReturnFailureWhenConfigNotFound() {
        // Given: no config found for the user
        when(adapterConfigRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        // When: testing connection with non-existent config
        final TestConnectionResponse response = adapterTestService.testConnection(99L, 1L);

        // Then: failure is returned, no exception thrown
        assertThat(response.isSuccess(), is(false));
        assertThat(response.getError(), is(notNullValue()));
        verifyNoInteractions(adapterRegistry);
    }

    // ========================= FACTORY METHODS =========================

    private static AdapterConfig aConfigForUser(final User user, final AdapterType adapterType) {
        final AdapterConfig config = new AdapterConfig();
        config.setUser(user);
        config.setAdapterType(adapterType);
        config.setLabel("Test Config");
        config.setConfig(Map.of("webhookUrl", "https://example.com/webhook/test"));
        config.setEnabled(true);
        return config;
    }
}
