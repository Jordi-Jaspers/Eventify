package org.jordijaspers.eventify.email.service.sender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.token.model.TokenType;
import org.jordijaspers.eventify.api.token.service.TokenService;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.email.model.MailMessage;
import org.jordijaspers.eventify.email.service.message.MailMessageFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A service to send emails in a development environment.
 */
@Slf4j
@Service
@Profile("development")
@RequiredArgsConstructor
public class DevelopmentEmailService implements EmailService {

    private static final String TOKEN = "token";

    private final TokenService tokenService;

    private final MailMessageFactory mailMessageFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendUserValidationEmail(final User recipient) {
        final Map<String, Object> variables = new ConcurrentHashMap<>();

        final Token token = tokenService.generateToken(recipient, TokenType.USER_VALIDATION_TOKEN);
        log.info("Sending user validation email with token '{}' to user '{}'.", token.getValue(), recipient.getEmail());
        variables.put(TOKEN, token.getValue());

        final MailMessage message = mailMessageFactory.createUserValidationMessage(variables);
        sendEmail(recipient, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPasswordResetEmail(final User recipient) {
        final Map<String, Object> variables = new ConcurrentHashMap<>();

        final Token resetToken = tokenService.generateToken(recipient, TokenType.RESET_PASSWORD_TOKEN);
        log.info("Sending password reset with token '{}' to user '{}'.", resetToken.getValue(), recipient.getEmail());
        variables.put(TOKEN, resetToken.getValue());

        final MailMessage message = mailMessageFactory.createPasswordResetMessage(variables);
        sendEmail(recipient, message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendEmail(final User recipient, final MailMessage mailMessage) {
        log.info("Sending email to user '{}' with content: \n{}", recipient.getEmail(), mailMessage);
    }
}
