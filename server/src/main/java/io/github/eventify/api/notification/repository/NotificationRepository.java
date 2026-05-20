package io.github.eventify.api.notification.repository;

import io.github.eventify.api.notification.model.Notification;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for notification persistence.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    /**
     * Counts unread notifications for a user.
     *
     * @param userId the user ID
     * @return count of unread notifications
     */
    long countByUserIdAndReadAtIsNull(Long userId);

    /**
     * Finds a notification by ID and user ID.
     *
     * @param id     the notification ID
     * @param userId the user ID
     * @return optional notification
     */
    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    /**
     * Marks all unread notifications as read for a user.
     *
     * @param userId the user ID
     * @param readAt the read timestamp
     * @return number of updated rows
     */
    @Modifying
    @Query("UPDATE Notification n SET n.readAt = :readAt WHERE n.user.id = :userId AND n.readAt IS NULL")
    int markAllAsReadForUser(@Param("userId") Long userId, @Param("readAt") OffsetDateTime readAt);
}
