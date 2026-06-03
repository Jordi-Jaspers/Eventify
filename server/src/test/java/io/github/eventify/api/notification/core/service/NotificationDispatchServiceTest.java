package io.github.eventify.api.notification.core.service;

import io.github.eventify.api.notification.adapter.AdapterRegistry;
import io.github.eventify.api.notification.adapter.NotificationAdapter;
import io.github.eventify.api.notification.adapter.model.AdapterType;
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
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Notification Dispatch Service")
public class NotificationDispatchServiceTest extends UnitTest {

    @Mock
    private AudienceResolver audienceResolver;

    @Mock
    private AdapterRegistry adapterRegistry;

    @Mock
    private NotificationAdapter inAppAdapter;

    private NotificationDispatchService dispatchService;

    @BeforeEach
    public void setUp() {
        dispatchService = new NotificationDispatchService(audienceResolver, adapterRegistry);
    }

    @Test
    @DisplayName("Should call adapter once when audience resolves to single user")
    public void shouldDispatchToSingleUser() {
        // Given: An audience that resolves to one user
        final User user = aValidUser();
        final NotificationAudience audience = NotificationAudience.user(user.getId());
        final NotificationPayload payload = aValidPayload();
        final List<AdapterType> targetAdapters = List.of(AdapterType.IN_APP);

        when(audienceResolver.resolve(audience)).thenReturn(List.of(user));
        when(adapterRegistry.filterByAdapterTypes(targetAdapters)).thenReturn(List.of(inAppAdapter));

        // When: Dispatching the notification
        dispatchService.dispatch(audience, payload, targetAdapters);

        // Then: The adapter should be called once with that user and payload
        verify(inAppAdapter, times(1)).send(user, payload);
    }

    @Test
    @DisplayName("Should not call adapter when audience resolves to empty list")
    public void shouldNotDispatchWhenNoRecipients() {
        // Given: An audience that resolves to no users
        final NotificationAudience audience = NotificationAudience.user(999L);
        final NotificationPayload payload = aValidPayload();
        final List<AdapterType> targetAdapters = List.of(AdapterType.IN_APP);

        when(audienceResolver.resolve(audience)).thenReturn(List.of());
        when(adapterRegistry.filterByAdapterTypes(targetAdapters)).thenReturn(List.of(inAppAdapter));

        // When: Dispatching the notification
        dispatchService.dispatch(audience, payload, targetAdapters);

        // Then: The adapter should never be called
        verify(inAppAdapter, never()).send(any(), any());
    }

    @Test
    @DisplayName("Should delegate only to adapters returned by AdapterRegistry")
    public void shouldDelegateToAllAdapters() {
        // Given: Two adapters filtered by AdapterRegistry
        final NotificationAdapter secondAdapter = mock(NotificationAdapter.class);
        final List<AdapterType> targetAdapters = List.of(AdapterType.IN_APP, AdapterType.MATTERMOST);

        final User user = aValidUser();
        final NotificationAudience audience = NotificationAudience.user(user.getId());
        final NotificationPayload payload = aValidPayload();

        when(audienceResolver.resolve(audience)).thenReturn(List.of(user));
        when(adapterRegistry.filterByAdapterTypes(targetAdapters)).thenReturn(List.of(inAppAdapter, secondAdapter));

        // When: Dispatching the notification
        dispatchService.dispatch(audience, payload, targetAdapters);

        // Then: Both adapters returned by registry should receive the notification
        verify(inAppAdapter, times(1)).send(user, payload);
        verify(secondAdapter, times(1)).send(user, payload);
    }

    @Test
    @DisplayName("Should invoke AdapterRegistry.filterByAdapterTypes with the provided targetAdapters")
    public void shouldFilterAdaptersViaRegistry() {
        // Given: A dispatch call with specific target adapters
        final List<AdapterType> targetAdapters = List.of(AdapterType.IN_APP, AdapterType.SLACK);
        final NotificationAudience audience = NotificationAudience.user(1L);
        final NotificationPayload payload = aValidPayload();

        when(audienceResolver.resolve(audience)).thenReturn(List.of(aValidUser()));
        when(adapterRegistry.filterByAdapterTypes(targetAdapters)).thenReturn(List.of(inAppAdapter));

        // When: Dispatching the notification
        dispatchService.dispatch(audience, payload, targetAdapters);

        // Then: Registry is called with the exact target adapter types
        verify(adapterRegistry, times(1)).filterByAdapterTypes(targetAdapters);
    }

    @Test
    @DisplayName("Should not invoke any adapter when targetAdapters list is empty")
    public void shouldNotDispatchWhenTargetAdaptersIsEmpty() {
        // Given: An empty targetAdapters list
        final NotificationAudience audience = NotificationAudience.user(1L);
        final NotificationPayload payload = aValidPayload();
        final List<AdapterType> targetAdapters = List.of();

        when(audienceResolver.resolve(audience)).thenReturn(List.of(aValidUser()));
        when(adapterRegistry.filterByAdapterTypes(targetAdapters)).thenReturn(List.of());

        // When: Dispatching the notification
        dispatchService.dispatch(audience, payload, targetAdapters);

        // Then: No adapter invoked
        verifyNoInteractions(inAppAdapter);
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

    // -------------------------------------------------------------------------
    // dispatchOrganizationStatusChange
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Organization Status Change Notifications")
    class OrganizationStatusChangeTests {

        @Test
        @DisplayName("Should dispatch urgent notification when status changes to SUSPENDED")
        void shouldDispatchUrgentNotificationOnSuspension() {
            // Given: An audience with one member and a registry returning the in-app adapter
            final User user = aValidUser();
            when(audienceResolver.resolve(any())).thenReturn(List.of(user));
            when(adapterRegistry.filterByAdapterTypes(any())).thenReturn(List.of(inAppAdapter));

            // When: Dispatching organization status change to SUSPENDED
            dispatchService.dispatchOrganizationStatusChange(1L, "Acme Corp", OrganizationStatus.ACTIVE, OrganizationStatus.SUSPENDED);

            // Then: In-app adapter sends the suspension payload
            final ArgumentCaptor<NotificationPayload> captor = ArgumentCaptor.forClass(NotificationPayload.class);
            verify(inAppAdapter).send(eq(user), captor.capture());
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
            // Given: An audience with one member and a registry returning the in-app adapter
            final User user = aValidUser();
            when(audienceResolver.resolve(any())).thenReturn(List.of(user));
            when(adapterRegistry.filterByAdapterTypes(any())).thenReturn(List.of(inAppAdapter));

            // When: Dispatching organization status change from SUSPENDED to ACTIVE
            dispatchService.dispatchOrganizationStatusChange(1L, "Acme Corp", OrganizationStatus.SUSPENDED, OrganizationStatus.ACTIVE);

            // Then: In-app adapter sends the reactivation payload
            final ArgumentCaptor<NotificationPayload> captor = ArgumentCaptor.forClass(NotificationPayload.class);
            verify(inAppAdapter).send(eq(user), captor.capture());
            final NotificationPayload payload = captor.getValue();
            assertThat(payload.getTitle(), is("Organization reactivated"));
            assertThat(payload.getMessage(), is("Acme Corp has been reactivated"));
            assertThat(payload.getCategory(), is(NotificationCategory.SYSTEM));
            assertThat(payload.isUrgent(), is(false));
        }

        @Test
        @DisplayName("Should not dispatch when status is idempotent ACTIVE to ACTIVE")
        void shouldNotDispatchOnIdempotentActive() {
            // Given / When: Dispatching with same ACTIVE status
            dispatchService.dispatchOrganizationStatusChange(1L, "Acme Corp", OrganizationStatus.ACTIVE, OrganizationStatus.ACTIVE);

            // Then: No interactions with any collaborator
            verifyNoInteractions(audienceResolver);
            verifyNoInteractions(adapterRegistry);
            verifyNoInteractions(inAppAdapter);
        }

        @Test
        @DisplayName("Should not dispatch when status is idempotent SUSPENDED to SUSPENDED")
        void shouldNotDispatchOnIdempotentSuspended() {
            // Given / When: Dispatching with same SUSPENDED status
            dispatchService.dispatchOrganizationStatusChange(1L, "Acme Corp", OrganizationStatus.SUSPENDED, OrganizationStatus.SUSPENDED);

            // Then: No interactions with any collaborator
            verifyNoInteractions(audienceResolver);
            verifyNoInteractions(adapterRegistry);
            verifyNoInteractions(inAppAdapter);
        }
    }
}
