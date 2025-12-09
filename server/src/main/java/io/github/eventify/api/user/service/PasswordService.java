package io.github.eventify.api.user.service;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.request.UpdatePasswordRequest;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.email.service.sender.EmailService;
import io.github.eventify.common.exception.PasswordIncorrectException;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * A service to manage the password of a user.
 */
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final TokenService tokenService;

    private final EmailService emailService;

    /**
     * Requests a password reset for the user with the given email.
     *
     * @param email the email of the user
     */
    public void requestPasswordReset(final String email) {
        userRepository.findByEmail(email).ifPresent(emailService::sendPasswordResetEmail);
    }

    /**
     * Updates the password of the user.
     *
     * @param request the request containing the old and new password
     * @param user    the user to update the password for
     */
    public void updatePassword(final UpdatePasswordRequest request, final User user) {
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new PasswordIncorrectException();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Changes the password of the user.
     *
     * @param password the new password
     * @param value    the value of the token
     */
    public void changePassword(final String password, final String value) {
        final Token token = tokenService.findPasswordResetTokenByValue(value);
        final User user = token.getUser();
        tokenService.invalidateTokensForUser(user, TokenType.RESET_PASSWORD_TOKEN);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }
}
