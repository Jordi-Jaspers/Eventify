package io.github.eventify.api.user.service;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.request.UpdatePasswordRequest;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.email.service.sender.EmailService;
import io.github.eventify.common.exception.PasswordIncorrectException;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static io.github.eventify.common.exception.ApiErrorCode.USER_NOT_FOUND_ERROR;

/**
 * Service responsible for password management operations including reset requests,
 * password updates, and forced password resets by administrators.
 */
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final TokenService tokenService;

    private final EmailService emailService;

    /**
     * Initiates a password reset flow for the given email address. If a user with the
     * given email exists, a password reset email is sent. No error is thrown if the user
     * does not exist (to prevent email enumeration).
     *
     * @param email the email address of the user requesting a password reset
     */
    public void requestPasswordReset(final String email) {
        userRepository.findByEmail(email).ifPresent(emailService::sendPasswordResetEmail);
    }

    /**
     * Updates the password for an authenticated user after verifying the current password.
     *
     * @param request the request containing the old and new passwords
     * @param user    the currently authenticated user
     * @throws PasswordIncorrectException if the provided old password does not match
     */
    public void updatePassword(final UpdatePasswordRequest request, final User user) {
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new PasswordIncorrectException();
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Completes the password reset flow by setting a new password using a valid reset token.
     * All existing reset tokens for the user are invalidated after the change.
     *
     * @param password the new password to set
     * @param value    the password reset token value
     */
    public void changePassword(final String password, final String value) {
        final Token token = tokenService.findPasswordResetTokenByValue(value);
        final User user = token.getUser();
        tokenService.invalidateTokensForUser(user, TokenType.RESET_PASSWORD_TOKEN);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    /**
     * Forces a password reset for the specified user. A random password is assigned,
     * all active tokens are invalidated, and a password reset email is sent to the user.
     * This is an administrative action.
     *
     * @param userId the ID of the user to force a password reset for
     * @throws DataNotFoundException if no user with the given ID exists
     */
    public void forcePasswordReset(final Long userId) {
        final User user = userRepository.findById(userId)
            .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND_ERROR));

        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        userRepository.save(user);

        tokenService.invalidateTokensForUser(user, TokenType.values());
        emailService.sendPasswordResetEmail(user);
    }
}
