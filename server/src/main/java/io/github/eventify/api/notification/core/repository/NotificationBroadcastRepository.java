package io.github.eventify.api.notification.core.repository;

import io.github.eventify.api.notification.core.model.NotificationBroadcast;
import io.github.eventify.api.notification.core.model.NotificationCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link NotificationBroadcast} entities.
 */
@Repository
public interface NotificationBroadcastRepository extends JpaRepository<NotificationBroadcast, Long>,
                                                 JpaSpecificationExecutor<NotificationBroadcast> {

    boolean existsByCategoryAndTitle(NotificationCategory category, String title);
}
