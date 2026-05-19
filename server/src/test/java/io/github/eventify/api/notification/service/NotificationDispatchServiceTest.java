package io.github.eventify.api.notification.service;

import io.github.eventify.api.notification.adapter.NotificationAdapter;
import io.github.eventify.api.notification.model.NotificationAudience;
import io.github.eventify.api.notification.model.NotificationCategory;
import io.github.eventify.api.notification.model.NotificationPayload;
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
    private NotificationAdapter inAppAdapter;

    private NotificationDispatchService dispatchService;

    @BeforeEach
    public void setUp() {
        dispatchService = new NotificationDispatchService(audienceResolver, List.of(inAppAdapter));
    }

    @Test
    @DisplayName("Should call adapter once when audience resolves to single user")
    public void shouldDispatchToSingleUser() {
        // Given: An audience that resolves to one user
        final User user = aValidUser();
        final NotificationAudience audience = NotificationAudience.user(user.getId());
        final NotificationPayload payload = aValidPayload();

        when(audienceResolver.resolve(audience)).thenReturn(List.of(user));

        // When: Dispatching the notification
        dispatchService.dispatch(audience, payload);

        // Then: The adapter should be called once with that user and payload
        verify(inAppAdapter, times(1)).send(user, payload);
    }

    @Test
    @DisplayName("Should not call adapter when audience resolves to empty list")
    public void shouldNotDispatchWhenNoRecipients() {
        // Given: An audience that resolves to no users
        final NotificationAudience audience = NotificationAudience.user(999L);
        final NotificationPayload payload = aValidPayload();

        when(audienceResolver.resolve(audience)).thenReturn(List.of());

        // When: Dispatching the notification
        dispatchService.dispatch(audience, payload);

        // Then: The adapter should never be called
        verifyNoInteractions(inAppAdapter);
    }

    @Test
    @DisplayName("Should delegate to all registered adapters")
    public void shouldDelegateToAllAdapters() {
        // Given: Two adapters registered
        final NotificationAdapter secondAdapter = mock(NotificationAdapter.class);
        final NotificationDispatchService serviceWithTwoAdapters = new NotificationDispatchService(
            audienceResolver,
            List.of(inAppAdapter, secondAdapter)
        );

        final User user = aValidUser();
        final NotificationAudience audience = NotificationAudience.user(user.getId());
        final NotificationPayload payload = aValidPayload();

        when(audienceResolver.resolve(audience)).thenReturn(List.of(user));

        // When: Dispatching the notification
        serviceWithTwoAdapters.dispatch(audience, payload);

        // Then: Both adapters should receive the notification
        verify(inAppAdapter, times(1)).send(user, payload);
        verify(secondAdapter, times(1)).send(user, payload);
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
            final User user = aValidUser();
            when(audienceResolver.resolve(any())).thenReturn(List.of(user));

            dispatchService.dispatchOrganizationStatusChange(1L, "Acme Corp", OrganizationStatus.ACTIVE, OrganizationStatus.SUSPENDED);

            final ArgumentCaptor<NotificationPayload> captor = ArgumentCaptor.forClass(NotificationPayload.class);
            verify(inAppAdapter).send(eq(user), captor.capture());
            final NotificationPayload payload = captor.getValue();
            assertThat(payload.getTitle(), is("Organization suspended"));
            assertThat(payload.getMessage(), is("Acme Corp has been suspended"));
            assertThat(payload.getCategory(), is(NotificationCategory.SYSTEM));
            assertThat(payload.getActionUrl(), is("/organizations"));
            assertThat(payload.isUrgent(), is(true));
        }

        @Test
        @DisplayName("Should dispatch non-urgent notification when status changes from SUSPENDED to ACTIVE")
        void shouldDispatchNonUrgentNotificationOnReactivation() {
            final User user = aValidUser();
            when(audienceResolver.resolve(any())).thenReturn(List.of(user));

            dispatchService.dispatchOrganizationStatusChange(1L, "Acme Corp", OrganizationStatus.SUSPENDED, OrganizationStatus.ACTIVE);

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
            dispatchService.dispatchOrganizationStatusChange(1L, "Acme Corp", OrganizationStatus.ACTIVE, OrganizationStatus.ACTIVE);

            verifyNoInteractions(audienceResolver);
            verifyNoInteractions(inAppAdapter);
        }

        @Test
        @DisplayName("Should not dispatch when status is idempotent SUSPENDED to SUSPENDED")
        void shouldNotDispatchOnIdempotentSuspended() {
            dispatchService.dispatchOrganizationStatusChange(1L, "Acme Corp", OrganizationStatus.SUSPENDED, OrganizationStatus.SUSPENDED);

            verifyNoInteractions(audienceResolver);
            verifyNoInteractions(inAppAdapter);
        }
    }
}
