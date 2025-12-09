package io.github.eventify.common.email.service.sender;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.email.model.MailMessage;
import io.github.eventify.common.email.service.message.MailMessageFactory;
import io.github.jframe.exception.JFrameException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static io.github.eventify.common.constant.Constants.EmailVariables.EMAIL_ADDRESS;
import static io.github.eventify.common.constant.Constants.EmailVariables.TOKEN;
import static java.util.Objects.isNull;

/**
 * A service to send emails in a development environment.
 */
@Slf4j
@Async
@Service
@Profile("!console")
@RequiredArgsConstructor
public class DefaultEmailService implements EmailService {

    private final TokenService tokenService;

    private final MailMessageFactory mailMessageFactory;

    private final JavaMailSender mailSender;

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
        final MimeMessagePreparator messagePreparator = mimeMessage -> {
            final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom(mailMessage.getFrom());
            messageHelper.setTo(recipient.getEmail());
            messageHelper.setSubject(mailMessage.getSubject());
            messageHelper.setText(mailMessage.getBody(), mailMessage.isHtml());
        };

        try {
            if (isNull(recipient) || StringUtils.isBlank(recipient.getEmail())) {
                log.warn("[MAIL SERVICE] No recipient was configured for email with subject '{}'.", mailMessage.getSubject());
                return;
            }

            log.info("[MAIL SERVICE] Sending email to '{}' with subject '{}'.", recipient, mailMessage.getSubject());
            mailSender.send(messagePreparator);
        } catch (final MailException exception) {
            throw new JFrameException("Something went wrong while sending email: " + exception.getMessage(), exception);
        }
    }
}
