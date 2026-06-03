package io.github.eventify.api.notification.core.service;

import io.github.eventify.api.notification.adapter.AdapterRegistry;
import io.github.eventify.api.notification.adapter.NotificationAdapter;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.core.model.NotificationAudience;
import io.github.eventify.api.notification.core.model.NotificationCategory;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * Service for dispatching notifications to audiences via registered adapters.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private static final String ORGANIZATIONS_URL = "/organizations";
    private static final String VIEW_ORGANIZATIONS_LABEL = "View organizations";

    private final AudienceResolver audienceResolver;

    private final AdapterRegistry adapterRegistry;

    /**
     * Dispatches a notification to all users in the audience via the specified adapters.
     *
     * @param audience       the target audience
     * @param payload        the notification payload
     * @param targetAdapters the list of adapter types to use
     */
    public void dispatch(final NotificationAudience audience, final NotificationPayload payload, final List<AdapterType> targetAdapters) {
        final List<User> recipients = audienceResolver.resolve(audience);
        final List<NotificationAdapter> adapters = adapterRegistry.filterByAdapterTypes(targetAdapters);
        for (final User recipient : recipients) {
            for (final NotificationAdapter adapter : adapters) {
                adapter.send(recipient, payload);
            }
        }
    }

    /**
     * Dispatches a notification to all users in the audience via the IN_APP adapter.
     *
     * @param audience the target audience
     * @param payload  the notification payload
     */
    public void dispatch(final NotificationAudience audience, final NotificationPayload payload) {
        dispatch(audience, payload, List.of(AdapterType.IN_APP));
    }

    /**
     * Dispatches a notification to organization members when org status changes to/from SUSPENDED.
     */
    public void dispatchOrganizationStatusChange(
        final Long orgId,
        final String orgName,
        final OrganizationStatus oldStatus,
        final OrganizationStatus newStatus
    ) {
        if (oldStatus != OrganizationStatus.SUSPENDED && newStatus == OrganizationStatus.SUSPENDED) {
            dispatchOrganizationSuspended(orgId, orgName);
        } else if (oldStatus == OrganizationStatus.SUSPENDED && newStatus != OrganizationStatus.SUSPENDED) {
            dispatchOrganizationReactivated(orgId, orgName);
        }
    }

    private void dispatchOrganizationSuspended(final Long orgId, final String orgName) {
        final NotificationPayload payload = buildOrganizationStatusPayload(
            "Organization suspended",
            orgName + " has been suspended. Contact a platform administrator for more information.",
            true
        );
        dispatch(NotificationAudience.organization(orgId), payload, List.of(AdapterType.IN_APP));
    }

    private void dispatchOrganizationReactivated(final Long orgId, final String orgName) {
        final NotificationPayload payload = buildOrganizationStatusPayload(
            "Organization reactivated",
            orgName + " has been reactivated",
            false
        );
        dispatch(NotificationAudience.organization(orgId), payload, List.of(AdapterType.IN_APP));
    }

    private NotificationPayload buildOrganizationStatusPayload(
        final String title,
        final String message,
        final boolean urgent
    ) {
        return new NotificationPayload(
            NotificationCategory.SYSTEM,
            title,
            message,
            ORGANIZATIONS_URL,
            VIEW_ORGANIZATIONS_LABEL,
            urgent,
            null
        );
    }

    /**
     * Dispatches a welcome notification to the given user. Swallows exceptions to avoid disrupting the registration flow.
     *
     * @param user the newly registered user
     */
    public void dispatchWelcomeNotification(final User user) {
        final NotificationPayload payload = new NotificationPayload(
            NotificationCategory.ANNOUNCEMENT,
            "Welcome to Eventify",
            "Get started by creating your first channel and setting up your first watchlist.",
            "/channels",
            "Get started",
            false,
            null
        );
        dispatch(NotificationAudience.user(user.getId()), payload, List.of(AdapterType.IN_APP));
    }
}
