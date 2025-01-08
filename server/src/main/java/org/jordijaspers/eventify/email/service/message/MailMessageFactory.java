package org.jordijaspers.eventify.email.service.message;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.jordijaspers.eventify.common.config.properties.ApplicationProperties;
import org.jordijaspers.eventify.email.config.properties.EmailProperties;
import org.jordijaspers.eventify.email.model.MailMessage;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Mail message factory to create thymeleaf messages.
 */
@Service
@RequiredArgsConstructor
public class MailMessageFactory {

    /**
     * The Application properties.
     */
    private final ApplicationProperties properties;

    /**
     * The email sending configuration properties.
     */
    private final EmailProperties emailProperties;

    /**
     * The Thymeleaf template engine.
     */
    private final TemplateEngine templateEngine;

    /**
     * Creates a new MailMessage for the user validation.
     */
    public MailMessage createUserValidationMessage(final Map<String, Object> variables) {
        final String subject = "[Eventify.io] Validate your account";
        return createMessage(subject, "account-created", variables);
    }

    /**
     * Creates a new MailMessage for the password reset.
     */
    public MailMessage createPasswordResetMessage(final Map<String, Object> variables) {
        final String subject = "[Eventify.io] Reset your password";
        return createMessage(subject, "password-reset", variables);
    }

    /**
     * Creates a new MailMessage.
     */
    private MailMessage createMessage(final String subject, final String template, final Map<String, Object> variables) {
        variables.put("applicationUrl", properties.getUrl());

        final Context context = new Context();
        context.setVariables(variables);
        final String payload = templateEngine.process(template, context);

        final MailMessage message = new MailMessage();
        message.setFrom(emailProperties.getAddress());
        message.setSubject(subject);
        message.setBody(payload);
        message.setHtml(true);
        return message;
    }
}
