package io.github.eventify.api.notification.core.repository;

import io.github.eventify.api.notification.core.model.Notification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
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
     * Finds recent notifications for a user created after the given threshold, limited by pageable.
     *
     * @param userId    the user ID
     * @param threshold the earliest createdAt to include
     * @param pageable  pagination (use to limit to max 10, ordered by createdAt desc)
     * @return list of recent notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.createdAt >= :threshold ORDER BY n.createdAt DESC")
    List<Notification> findRecentByUserId(@Param("userId") Long userId, @Param("threshold") OffsetDateTime threshold, Pageable pageable);

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
