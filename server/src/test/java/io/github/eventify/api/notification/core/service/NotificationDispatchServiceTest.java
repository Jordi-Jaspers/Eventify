package io.github.eventify.api.notification.core.service;

import io.github.eventify.api.notification.adapter.AdapterRegistry;
import io.github.eventify.api.notification.adapter.adapters.NotificationAdapter;
import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.adapter.repository.AdapterConfigRepository;
import io.github.eventify.api.notification.adapter.service.AdapterSendGateway;
import io.github.eventify.api.notification.core.model.NotificationAudience;
import io.github.eventify.api.notification.core.model.NotificationCategory;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.user.model.User;
import io.github.eventify.support.UnitTest;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@DisplayName("Unit Test - Notification Dispatch Service")
public class NotificationDispatchServiceTest extends UnitTest {

    @Mock
    private AudienceResolver audienceResolver;

    @Mock
    private AdapterRegistry adapterRegistry;

    @Mock
    private AdapterSendGateway adapterSendGateway;

    @Mock
    private AdapterConfigRepository adapterConfigRepository;

    @Mock
    private NotificationAdapter inAppAdapter;

    private NotificationDispatchService dispatchService;

    @BeforeEach
    public void setUp() {
        lenient().when(inAppAdapter.getAdapterType()).thenReturn(AdapterType.IN_APP);
        lenient().when(adapterConfigRepository.findByUserIdAndOrganizationIdIsNull(anyLong())).thenReturn(List.of());
        dispatchService = new NotificationDispatchService(audienceResolver, adapterRegistry, adapterSendGateway, adapterConfigRepository);
    }

    @Test
    @DisplayName("Should sendAsync once when audience resolves to single user")
    public void shouldDispatchToSingleUser() {
        final User user = aValidUser();
        final NotificationAudience audience = NotificationAudience.user(user.getId());
        final NotificationPayload payload = aValidPayload();
        final List<AdapterType> targetAdapters = List.of(AdapterType.IN_APP);

        when(audienceResolver.resolve(audience)).thenReturn(List.of(user));
        when(adapterRegistry.filterByAdapterTypes(targetAdapters)).thenReturn(List.of(inAppAdapter));

        dispatchService.dispatch(audience, payload, targetAdapters);

        verify(adapterSendGateway, times(1)).sendAsync(eq(inAppAdapter), eq(user), eq(payload), isNull());
    }

    @Test
    @DisplayName("Should not sendAsync when audience resolves to empty list")
    public void shouldNotDispatchWhenNoRecipients() {
        final NotificationAudience audience = NotificationAudience.user(999L);
        final NotificationPayload payload = aValidPayload();
        final List<AdapterType> targetAdapters = List.of(AdapterType.IN_APP);

        when(audienceResolver.resolve(audience)).thenReturn(List.of());
        when(adapterRegistry.filterByAdapterTypes(targetAdapters)).thenReturn(List.of(inAppAdapter));

        dispatchService.dispatch(audience, payload, targetAdapters);

        verifyNoInteractions(adapterSendGateway);
    }

    @Test
    @DisplayName("Should delegate only to adapters returned by AdapterRegistry")
    public void shouldDelegateToAllAdapters() {
        final NotificationAdapter mattermostAdapter = mock(NotificationAdapter.class);
        when(mattermostAdapter.getAdapterType()).thenReturn(AdapterType.MATTERMOST);
        final AdapterConfig mattermostConfig = new AdapterConfig();
        mattermostConfig.setAdapterType(AdapterType.MATTERMOST);
        mattermostConfig.setEnabled(true);

        final List<AdapterType> targetAdapters = List.of(AdapterType.IN_APP, AdapterType.MATTERMOST);
        final User user = aValidUser();
        final NotificationAudience audience = NotificationAudience.user(user.getId());
        final NotificationPayload payload = aValidPayload();

        when(audienceResolver.resolve(audience)).thenReturn(List.of(user));
        when(adapterRegistry.filterByAdapterTypes(targetAdapters)).thenReturn(List.of(inAppAdapter, mattermostAdapter));
        when(adapterConfigRepository.findByUserIdAndOrganizationIdIsNull(user.getId()))
            .thenReturn(List.of(mattermostConfig));

        dispatchService.dispatch(audience, payload, targetAdapters);

        verify(adapterSendGateway).sendAsync(eq(inAppAdapter), eq(user), eq(payload), isNull());
        verify(adapterSendGateway).sendAsync(eq(mattermostAdapter), eq(user), eq(payload), eq(mattermostConfig));
        verifyNoMoreInteractions(adapterSendGateway);
    }

    @Test
    @DisplayName("Should invoke AdapterRegistry.filterByAdapterTypes with the provided targetAdapters")
    public void shouldFilterAdaptersViaRegistry() {
        final List<AdapterType> targetAdapters = List.of(AdapterType.IN_APP, AdapterType.SLACK);
        final NotificationAudience audience = NotificationAudience.user(1L);
        final NotificationPayload payload = aValidPayload();

        when(audienceResolver.resolve(audience)).thenReturn(List.of(aValidUser()));
        when(adapterRegistry.filterByAdapterTypes(targetAdapters)).thenReturn(List.of(inAppAdapter));

        dispatchService.dispatch(audience, payload, targetAdapters);

        verify(adapterRegistry, times(1)).filterByAdapterTypes(targetAdapters);
    }

    @Test
    @DisplayName("Should not invoke any adapter when targetAdapters list is empty")
    public void shouldNotDispatchWhenTargetAdaptersIsEmpty() {
        final NotificationAudience audience = NotificationAudience.user(1L);
        final NotificationPayload payload = aValidPayload();
        final List<AdapterType> targetAdapters = List.of();

        when(audienceResolver.resolve(audience)).thenReturn(List.of(aValidUser()));
        when(adapterRegistry.filterByAdapterTypes(targetAdapters)).thenReturn(List.of());

        dispatchService.dispatch(audience, payload, targetAdapters);

        verifyNoInteractions(adapterSendGateway);
    }

    // ========================= FACTORY METHODS =========================

    private static NotificationPayload aValidPayload() {
        return new NotificationPayload(
            NotificationCategory.ANNOUNCEMENT,
            "Welcome to Eventify",
            "Get started by creating your first channel",
            "/channels",
            "Get started",
            false,
            null
        );
    }

    private static AdapterConfig anAdapterConfig(final Long id, final AdapterType type, final boolean enabled) {
        final AdapterConfig config = new AdapterConfig();
        config.setId(id);
        config.setAdapterType(type);
        config.setEnabled(enabled);
        return config;
    }

    // -------------------------------------------------------------------------
    // dispatchByAdapterConfigIds
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Dispatch By Adapter Config IDs")
    class DispatchByAdapterConfigIdsTests {

        @Test
        @DisplayName("Should dispatch via adapter types resolved from config IDs")
        void shouldDispatchViaResolvedAdapterTypes() {
            // Given
            final User user = aValidUser();
            final NotificationAudience audience = NotificationAudience.user(user.getId());
            final NotificationPayload payload = aValidPayload();
            final AdapterConfig inAppConfig = anAdapterConfig(1L, AdapterType.IN_APP, true);
            final List<Long> configIds = List.of(1L);

            when(adapterConfigRepository.findAllById(configIds)).thenReturn(List.of(inAppConfig));
            when(audienceResolver.resolve(audience)).thenReturn(List.of(user));
            when(adapterRegistry.filterByAdapterTypes(List.of(AdapterType.IN_APP))).thenReturn(List.of(inAppAdapter));

            // When
            dispatchService.dispatchByAdapterConfigIds(audience, payload, configIds);

            // Then
            verify(adapterSendGateway).sendAsync(eq(inAppAdapter), eq(user), eq(payload), isNull());
        }

        @Test
        @DisplayName("Should skip disabled configs when resolving adapter types")
        void shouldSkipDisabledConfigs() {
            // Given
            final NotificationAudience audience = NotificationAudience.user(1L);
            final NotificationPayload payload = aValidPayload();
            final AdapterConfig disabledConfig = anAdapterConfig(1L, AdapterType.IN_APP, false);
            final List<Long> configIds = List.of(1L);

            when(adapterConfigRepository.findAllById(configIds)).thenReturn(List.of(disabledConfig));
            when(audienceResolver.resolve(audience)).thenReturn(List.of(aValidUser()));
            when(adapterRegistry.filterByAdapterTypes(List.of())).thenReturn(List.of());

            // When
            dispatchService.dispatchByAdapterConfigIds(audience, payload, configIds);

            // Then: no adapters dispatched
            verifyNoInteractions(adapterSendGateway);
        }

        @Test
        @DisplayName("Should deduplicate adapter types when multiple configs resolve to same type")
        void shouldDeduplicateAdapterTypes() {
            // Given
            final NotificationAudience audience = NotificationAudience.user(1L);
            final NotificationPayload payload = aValidPayload();
            final AdapterConfig config1 = anAdapterConfig(1L, AdapterType.IN_APP, true);
            final AdapterConfig config2 = anAdapterConfig(2L, AdapterType.IN_APP, true);
            final List<Long> configIds = List.of(1L, 2L);

            when(adapterConfigRepository.findAllById(configIds)).thenReturn(List.of(config1, config2));
            when(audienceResolver.resolve(audience)).thenReturn(List.of(aValidUser()));
            when(adapterRegistry.filterByAdapterTypes(List.of(AdapterType.IN_APP))).thenReturn(List.of(inAppAdapter));

            // When
            dispatchService.dispatchByAdapterConfigIds(audience, payload, configIds);

            // Then: filterByAdapterTypes called with deduplicated list
            verify(adapterRegistry).filterByAdapterTypes(List.of(AdapterType.IN_APP));
        }

        @Test
        @DisplayName("Should not dispatch when config IDs list is empty")
        void shouldNotDispatchWhenConfigIdsIsEmpty() {
            // Given
            final NotificationAudience audience = NotificationAudience.user(1L);
            final NotificationPayload payload = aValidPayload();

            when(adapterConfigRepository.findAllById(List.of())).thenReturn(List.of());
            when(audienceResolver.resolve(audience)).thenReturn(List.of(aValidUser()));
            when(adapterRegistry.filterByAdapterTypes(List.of())).thenReturn(List.of());

            // When
            dispatchService.dispatchByAdapterConfigIds(audience, payload, List.of());

            // Then
            verifyNoInteractions(adapterSendGateway);
        }
    }

    // -------------------------------------------------------------------------
    // dispatchOrganizationStatusChange
    // -------------------------------------------------------------------------


    @Nested
    @DisplayName("Organization Status Change Notifications")
    class OrganizationStatusChangeTests {

        @Test
        @DisplayName("Should dispatch urgent notification when status changes to SUSPENDED")
        void shouldDispatchUrgentNotificationOnSuspension() {
            final User user = aValidUser();
            when(audienceResolver.resolve(any())).thenReturn(List.of(user));
            when(adapterRegistry.filterByAdapterTypes(any())).thenReturn(List.of(inAppAdapter));

            dispatchService.dispatchOrganizationStatusChange(1L, "Acme Corp", OrganizationStatus.ACTIVE, OrganizationStatus.SUSPENDED);

            final ArgumentCaptor<NotificationPayload> captor = ArgumentCaptor.forClass(NotificationPayload.class);
            verify(adapterSendGateway).sendAsync(eq(inAppAdapter), eq(user), captor.capture(), isNull());
            final NotificationPayload payload = captor.getValue();
            assertThat(payload.getTitle(), is("Organization suspended"));
            assertThat(payload.getMessage(), is("Acme Corp has been suspended. Contact a platform administrator for more information."));
            assertThat(payload.getCategory(), is(NotificationCategory.SYSTEM));
            assertThat(payload.getActionUrl(), is("/organizations"));
            assertThat(payload.isUrgent(), is(true));
        }

        @Test
        @DisplayName("Should dispatch non-urgent notification when status changes from SUSPENDED to ACTIVE")
        void shouldDispatchNonUrgentNotificationOnReactivation() {
            final User user = aValidUser();
            when(audienceResolver.resolve(any())).thenReturn(List.of(user));
            when(adapterRegistry.filterByAdapterTypes(any())).thenReturn(List.of(inAppAdapter));

            dispatchService.dispatchOrganizationStatusChange(1L, "Acme Corp", OrganizationStatus.SUSPENDED, OrganizationStatus.ACTIVE);

            final ArgumentCaptor<NotificationPayload> captor = ArgumentCaptor.forClass(NotificationPayload.class);
            verify(adapterSendGateway).sendAsync(eq(inAppAdapter), eq(user), captor.capture(), isNull());
            final NotificationPayload payload = captor.getValue();
            assertThat(payload.getTitle(), is("Organization reactivated"));
            assertThat(payload.getMessage(), is("Acme Corp has been reactivated"));
            assertThat(payload.getCategory(), is(NotificationCategory.SYSTEM));
            assertThat(payload.isUrgent(), is(false));
        }

        @Test
        @DisplayName("Should not dispatch when status is idempotent ACTIVE to ACTIVE")
        void shouldNotDispatchOnIdempotentActive() {
            dispatchService.dispatchOrganizationStatusChange(1L, "Acme Corp", OrganizationStatus.ACTIVE, OrganizationStatus.ACTIVE);

            verifyNoInteractions(audienceResolver);
            verifyNoInteractions(adapterRegistry);
            verifyNoInteractions(adapterSendGateway);
        }

        @Test
        @DisplayName("Should not dispatch when status is idempotent SUSPENDED to SUSPENDED")
        void shouldNotDispatchOnIdempotentSuspended() {
            dispatchService.dispatchOrganizationStatusChange(1L, "Acme Corp", OrganizationStatus.SUSPENDED, OrganizationStatus.SUSPENDED);

            verifyNoInteractions(audienceResolver);
            verifyNoInteractions(adapterRegistry);
            verifyNoInteractions(adapterSendGateway);
        }
    }
}
