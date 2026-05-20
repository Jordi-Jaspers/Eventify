package io.github.eventify.api.notification.model;

import io.github.eventify.api.authentication.model.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents the target audience for a notification dispatch.
 */
@Getter
@RequiredArgsConstructor
public final class NotificationAudience {

    private final NotificationAudienceType type;
    private final Long userId;
    private final Long organizationId;
    private final Role role;

    /**
     * Creates an audience targeting a single user by ID.
     *
     * @param userId the user ID
     * @return the audience
     */
    public static NotificationAudience user(final Long userId) {
        return new NotificationAudience(NotificationAudienceType.USER, userId, null, null);
    }

    /**
     * Creates an audience targeting all users.
     *
     * @return the audience
     */
    public static NotificationAudience allUsers() {
        return new NotificationAudience(NotificationAudienceType.ALL_USERS, null, null, null);
    }

    /**
     * Creates an audience targeting all members of an organization.
     *
     * @param organizationId the organization ID
     * @return the audience
     */
    public static NotificationAudience organization(final Long organizationId) {
        return new NotificationAudience(NotificationAudienceType.ORGANIZATION, null, organizationId, null);
    }

    /**
     * Creates an audience targeting all organization owners (distinct).
     *
     * @return the audience
     */
    public static NotificationAudience allOrganizationOwners() {
        return new NotificationAudience(NotificationAudienceType.ALL_ORGANIZATION_OWNERS, null, null, null);
    }

    /**
     * Creates an audience targeting all users with a specific global role.
     *
     * @param role the role
     * @return the audience
     */
    public static NotificationAudience globalRole(final Role role) {
        return new NotificationAudience(NotificationAudienceType.GLOBAL_ROLE, null, null, role);
    }
}
