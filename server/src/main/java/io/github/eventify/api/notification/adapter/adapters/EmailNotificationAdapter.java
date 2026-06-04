package io.github.eventify.api.notification.adapter.adapters;

import io.github.eventify.api.notification.adapter.model.AdapterConfig;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.email.service.sender.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

/**
 * Notification adapter for email delivery.
 * Sends HTML notification emails via {@link EmailService} to users with verified email addresses.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationAdapter implements NotificationAdapter {

    private final EmailService emailService;

    @Override
    public void send(final User user, final NotificationPayload payload, final AdapterConfig config) {
        if (!user.isValidated()) {
            log.warn("Skipping email notification for user '{}' — email not verified", user.getId());
            return;
        }
        emailService.sendNotificationEmail(user, payload);
    }

    @Override
    public AdapterType getAdapterType() {
        return AdapterType.EMAIL;
    }
}
