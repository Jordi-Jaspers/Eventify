package io.github.eventify.api.notification.model;

/**
 * Represents the target audience for a notification dispatch.
 */
public final class NotificationAudience {

    private final Long userId;

    private NotificationAudience(final Long userId) {
        this.userId = userId;
    }

    /**
     * Creates an audience targeting a single user by ID.
     *
     * @param userId the user ID
     * @return the audience
     */
    public static NotificationAudience user(final Long userId) {
        return new NotificationAudience(userId);
    }

    /**
     * Returns the user ID for this audience.
     *
     * @return the user ID
     */
    public Long getUserId() {
        return userId;
    }
}
