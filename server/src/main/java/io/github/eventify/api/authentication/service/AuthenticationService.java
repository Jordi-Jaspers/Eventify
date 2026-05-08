package io.github.eventify.api.authentication.service;

import io.github.eventify.api.authentication.model.request.LoginRequest;
import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.service.TokenService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.common.exception.AuthorizationException;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.github.jframe.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.UUID;
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
     * Authorizes the user with the given login request.
     *
     * @param request     the login request containing email, password, and rememberMe flag
     * @param httpRequest the HTTP request for device info capture
     * @return the user with the updated tokens
     */
    public User authorize(final LoginRequest request, final HttpServletRequest httpRequest) {
        final String username = request.getEmail();
        authenticate(username, request.getPassword());
        final User user = userService.loadUserByUsername(username);
        user.setLastLogin(now());
        userService.updateUserDetails(user);

        log.info("User '{}' successfully authenticated", username);
        final UUID familyId = cookieService.readDeviceId(httpRequest).orElse(UUID.randomUUID());
        return tokenService.generateAuthorizationTokens(user, httpRequest, request.isRememberMe(), familyId);
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
        user = userService.updateUserDetails(user);
        log.info("User '{}' successfully validated", user.getEmail());
        // Invalidate any existing refresh tokens before creating a new one for the verified session
        tokenService.invalidateTokensForUser(user, REFRESH_TOKEN);
        // Email verification flow has no remember-me concept; always issue a standard refresh token.
        final UUID familyId = cookieService.readDeviceId(request).orElse(UUID.randomUUID());
        return tokenService.generateAuthorizationTokens(user, request, false, familyId);
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
        resolveRefreshTokenId(principal, httpRequest).ifPresentOrElse(
            id -> {
                tokenService.deleteById(id);
                log.debug("Revoked refresh token id '{}'", id);
            },
            () -> log.warn("Logout called without a resolvable refresh token")
        );
    }

    private Optional<Long> resolveRefreshTokenId(final UserTokenPrincipal principal, final HttpServletRequest request) {
        if (principal != null && principal.getRefreshTokenId() != null) {
            return Optional.of(principal.getRefreshTokenId());
        }
        log.warn("Logout fallback: resolving refresh token id from cookie");
        return cookieService.readRefreshTokenValue(request).flatMap(this::resolveTokenIdFromValue);
    }

    private Optional<Long> resolveTokenIdFromValue(final String value) {
        try {
            return Optional.ofNullable(tokenService.findAuthorizationTokenByValue(value))
                .map(Token::getId);
        } catch (final ApiException ex) {
            return Optional.empty();
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
