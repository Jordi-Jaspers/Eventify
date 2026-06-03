package io.github.eventify.api.notification.adapter;

import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.core.model.Notification;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.notification.core.repository.NotificationRepository;
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
    public AdapterType getAdapterType() {
        return AdapterType.IN_APP;
    }

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
        if (payload.getBroadcast() != null) {
            notification.setBroadcast(payload.getBroadcast());
        }
        notificationRepository.save(notification);
        log.debug("In-app notification saved for user {}: {}", user.getId(), payload.getTitle());
    }
}
