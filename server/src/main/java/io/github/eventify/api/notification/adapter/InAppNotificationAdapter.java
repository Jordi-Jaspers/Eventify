package io.github.eventify.api.notification.adapter;

import io.github.eventify.api.notification.model.Notification;
import io.github.eventify.api.notification.model.NotificationPayload;
import io.github.eventify.api.notification.repository.NotificationRepository;
import io.github.eventify.api.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * In-app notification adapter that persists notifications to the database.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InAppNotificationAdapter implements NotificationAdapter {

    private final NotificationRepository notificationRepository;

    @Override
    public void send(final User user, final NotificationPayload payload) {
        final Notification notification = new Notification(
            user,
            payload.getCategory(),
            payload.getTitle(),
            payload.getMessage(),
            payload.getActionUrl(),
            payload.getActionLabel(),
            payload.isUrgent()
        );
        notificationRepository.save(notification);
        log.debug("In-app notification saved for user {}: {}", user.getId(), payload.getTitle());
    }
}
