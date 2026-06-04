package io.github.eventify.common.email.service.sender;



import io.github.eventify.api.notification.core.model.NotificationPayload;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.email.model.MailMessage;

/**
 * An interface for sending emails.
 */
public interface EmailService {

    /**
     * Send a predefined email to the user with the validation code.
     */
    void sendUserValidationEmail(User recipient);

    /**
     * Sends an email containing a url with a token to reset the password.
     */
    void sendPasswordResetEmail(User recipient);

    /**
     * Sends a notification alert email to the user using the notification-alert template.
     */
    void sendNotificationEmail(User recipient, NotificationPayload payload);

    /**
     * Send an email as defined in {@code mailMessage}.
     */
    void sendEmail(User recipient, MailMessage mailMessage);
}
