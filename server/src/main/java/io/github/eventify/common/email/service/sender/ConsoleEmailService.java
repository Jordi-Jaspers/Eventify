package io.github.eventify.common.email.service.sender;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.email.model.MailMessage;
import io.github.eventify.common.email.service.message.MailMessageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static io.github.eventify.common.constant.Constants.EmailVariables.EMAIL_ADDRESS;
import static io.github.eventify.common.constant.Constants.EmailVariables.TOKEN;

/**
 * A service to send emails in a development environment.
 */
@Slf4j
@Service
@Profile("console")
@RequiredArgsConstructor
public class ConsoleEmailService implements EmailService {

    private final TokenService tokenService;

    private final MailMessageFactory mailMessageFactory;

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendPasswordResetEmail(final User recipient) {
        final Map<String, Object> variables = new ConcurrentHashMap<>();

        final Token resetToken = tokenService.generateToken(recipient, TokenType.RESET_PASSWORD_TOKEN);
        log.info("Sending password reset with token '{}' to user '{}'.", resetToken.getValue(), recipient.getEmail());
        variables.put(TOKEN, resetToken.getValue());
        variables.put(EMAIL_ADDRESS, recipient.getEmail());

        final MailMessage message = mailMessageFactory.createPasswordResetMessage(variables);
        sendEmail(recipient, message);
    }

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
    public void sendEmail(final User recipient, final MailMessage mailMessage) {
        log.info("Sending email to user '{}' with content: \n{}", recipient.getEmail(), mailMessage);
    }
}
