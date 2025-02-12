package org.jordijaspers.eventify.api.authentication.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.jordijaspers.eventify.api.token.service.TokenService;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.service.UserService;
import org.jordijaspers.eventify.common.exception.AuthorizationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import static org.jordijaspers.eventify.api.token.model.TokenType.REFRESH_TOKEN;
import static org.jordijaspers.eventify.api.token.model.TokenType.USER_VALIDATION_TOKEN;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.INVALID_CREDENTIALS;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.USER_LOCKED_ERROR;
import static org.jordijaspers.eventify.common.util.SecurityUtil.getLoggedInUser;

/**
 * A service to manage authentication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final UserService userService;

    private final TokenService tokenService;

    /**
     * Registers a new user.
     *
     * @param user     the user to register
     * @param password the password of the user
     * @return the registered user
     */
    public User register(final User user, final String password) {
        log.info("Registering new user '{}'.", user.getUsername());
        return userService.registerAndNotify(user, password);
    }

    /**
     * Authorizes the user with the given username and password.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return the user with the updated tokens
     */
    public User authorize(final String username, final String password) {
        authenticate(username, password);
        final User user = userService.loadUserByUsername(username);
        user.setLastLogin(LocalDateTime.now());
        userService.updateUserDetails(user);

        log.info("User '{}' successfully authenticated", username);
        return tokenService.generateAuthorizationTokens(user);
    }

    /**
     * Verifies the email address of the user.
     *
     * @param token the token to verify the email address
     * @return the user with the updated email address
     */
    public User verifyEmail(final String token) {
        log.info("Attempting to validate user with token '{}'", token);
        User user = tokenService.findValidationTokenByValue(token).getUser();
        tokenService.invalidateTokensForUser(user, USER_VALIDATION_TOKEN);

        user.setValidated(true);
        user.setLastLogin(user.getLastLogin());
        user = userService.updateUserDetails(user);
        log.info("User '{}' successfully validated", user.getEmail());
        return tokenService.generateAuthorizationTokens(user);
    }

    /**
     * Resends the verification email.
     *
     * @param email the email address
     */
    public void resendVerification(final String email) {
        userService.resendVerificationEmail(email);
    }

    /**
     * Refreshes the access token using the refresh token.
     *
     * @param refreshToken the refresh token
     * @return the user with refreshed tokens
     */
    public User refresh(final String refreshToken) {
        return tokenService.refresh(refreshToken);
    }

    /**
     * Logs out the user by invalidating all refresh tokens.
     */
    public void logout() {
        try {
            final User user = getLoggedInUser();
            tokenService.invalidateTokensForUser(user, REFRESH_TOKEN);
            log.debug("User '{}' successfully logged out", user.getUsername());
        } catch (final AuthorizationException exception) {
            log.warn("Cannot log out user, no user is logged in");
        }
    }

    private void authenticate(final String username, final String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (final AuthenticationException exception) {
            log.error("Authorization failed for specified user '{}'", username);
            if (exception instanceof LockedException) {
                throw new AuthorizationException(USER_LOCKED_ERROR, exception);
            }
            throw new AuthorizationException(INVALID_CREDENTIALS, exception);
        }
    }
}
