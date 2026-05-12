package io.github.eventify.api.notification.repository;

import io.github.eventify.api.notification.model.NotificationBroadcast;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link NotificationBroadcast} entities.
 */
@Repository
public interface NotificationBroadcastRepository extends JpaRepository<NotificationBroadcast, Long>,
                                                 JpaSpecificationExecutor<NotificationBroadcast> {
}
