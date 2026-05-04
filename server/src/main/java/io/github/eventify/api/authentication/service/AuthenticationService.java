package io.github.eventify.api.authentication.service;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.common.exception.AuthorizationException;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.github.jframe.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import static io.github.eventify.api.token.model.TokenType.REFRESH_TOKEN;
import static io.github.eventify.api.token.model.TokenType.USER_VALIDATION_TOKEN;
import static io.github.eventify.common.exception.ApiErrorCode.INVALID_CREDENTIALS;
import static io.github.eventify.common.exception.ApiErrorCode.USER_LOCKED_ERROR;
import static io.github.eventify.common.util.TimeProvider.now;

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

    private final CookieService cookieService;

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
     * @param request  the HTTP request for device info capture
     * @return the user with the updated tokens
     */
    public User authorize(final String username, final String password, final HttpServletRequest request) {
        authenticate(username, password);
        final User user = userService.loadUserByUsername(username);
        user.setLastLogin(now());
        userService.updateUserDetails(user);

        log.info("User '{}' successfully authenticated", username);
        return tokenService.generateAuthorizationTokens(user, request);
    }

    /**
     * Verifies the email address of the user.
     *
     * @param token   the token to verify the email address
     * @param request the HTTP request for device info capture
     * @return the user with the updated email address
     */
    public User verifyEmail(final String token, final HttpServletRequest request) {
        log.info("Attempting to validate user with token '{}'", token);
        User user = tokenService.findValidationTokenByValue(token).getUser();
        tokenService.invalidateTokensForUser(user, USER_VALIDATION_TOKEN);

        user.setValidated(true);
        user.setLastLogin(user.getLastLogin());
        user = userService.updateUserDetails(user);
        log.info("User '{}' successfully validated", user.getEmail());
        // Invalidate any existing refresh tokens before creating a new one for the verified session
        tokenService.invalidateTokensForUser(user, REFRESH_TOKEN);
        return tokenService.generateAuthorizationTokens(user, request);
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
     * @param request      the HTTP request (used by the token service for activity tracking)
     * @return the user with refreshed tokens
     */
    public User refresh(final String refreshToken, final HttpServletRequest request) {
        return tokenService.refresh(refreshToken, request);
    }

    /**
     * Logs out the user by invalidating only the current refresh token. Falls back to the refresh-token cookie when
     * the principal carries no {@code refreshTokenId}. No-op when neither source provides a token to revoke.
     *
     * @param principal   the authenticated principal whose session should be revoked
     * @param httpRequest the current HTTP request (used for cookie fallback)
     */
    public void logout(final UserTokenPrincipal principal, final HttpServletRequest httpRequest) {
        Long refreshTokenId = principal == null ? null : principal.getRefreshTokenId();
        if (refreshTokenId == null) {
            // Fallback: extract from cookie directly. Handles cases where the principal arrived
            // without a populated id (e.g. immediately after a refresh, or browser-cookie edge cases).
            refreshTokenId = cookieService.readRefreshTokenValue(httpRequest)
                .map(this::resolveTokenIdFromValue)
                .orElse(null);
            if (refreshTokenId != null) {
                log.warn("Logout fallback: resolved refresh token id from cookie because principal did not carry it");
            }
        }
        if (refreshTokenId == null) {
            log.warn("Logout called without a resolvable refresh token");
            return;
        }
        tokenService.deleteById(refreshTokenId);
        log.debug("Revoked refresh token id '{}'", refreshTokenId);
    }

    private Long resolveTokenIdFromValue(final String value) {
        try {
            final Token token = tokenService.findAuthorizationTokenByValue(value);
            return token != null ? token.getId() : null;
        } catch (final ApiException ex) {
            return null;
        }
    }

    private void authenticate(final String username, final String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (final LockedException lockedException) {
            log.error("User account '{}' is locked", username);
            throw new AuthorizationException(USER_LOCKED_ERROR, lockedException);
        } catch (final AuthenticationException exception) {
            log.error("Authorization failed for specified user '{}'", username);
            throw new AuthorizationException(INVALID_CREDENTIALS, exception);
        }
    }
}
