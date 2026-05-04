package io.github.eventify.api.token.service;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.token.repository.TokenRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.InvalidJwtException;
import io.github.eventify.common.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.api.token.model.TokenType.*;
import static io.github.eventify.common.exception.ApiErrorCode.TOKEN_NOT_FOUND_ERROR;
import static io.github.eventify.common.util.TimeProvider.now;

/**
 * A service class which interacts with the token table in the database.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    private final JwtService jwtService;

    /**
     * Generate authorization tokens for a user, capturing device info from the request.
     * Each call creates a new session without invalidating existing sessions.
     *
     * @param user    the user to generate tokens for
     * @param request the HTTP request to extract device info from (may be null)
     * @return the user with new tokens set
     */
    public User generateAuthorizationTokens(final User user, final HttpServletRequest request) {
        final List<Token> existingSessions = tokenRepository.findByEmail(user.getEmail());
        log.debug("User '{}' has {} existing session(s); creating new session", user.getEmail(), existingSessions.size());

        final Token accessToken = jwtService.generateAccessToken(user);
        final Token newRefreshToken = jwtService.generateRefreshToken(user);
        newRefreshToken.captureDeviceMetadata(request);

        final Token savedRefreshToken = tokenRepository.save(newRefreshToken);
        log.info("Generated Access & Refresh tokens for user '{}'", user.getEmail());

        user.setRefreshToken(savedRefreshToken);
        user.setAccessToken(accessToken);
        return user;
    }

    /**
     * Refreshes the access token using the refresh token, preserving device info on the rotated token.
     *
     * @param refreshToken the refresh token value
     * @param request      the HTTP request (used to update lastActiveAt)
     * @return the user with refreshed tokens
     */
    public User refresh(final String refreshToken, final HttpServletRequest request) {
        final Token existingToken = findAuthorizationTokenByValue(refreshToken);
        if (existingToken == null) {
            throw new InvalidJwtException();
        }

        final User user = existingToken.getUser();
        log.info("Refreshing tokens for user '{}'", user.getUsername());

        final Token accessToken = jwtService.generateAccessToken(user);
        final Token newRefreshToken = jwtService.generateRefreshToken(user);
        newRefreshToken.inheritDeviceMetadataFrom(existingToken);
        newRefreshToken.setLastActiveAt(now());

        // Delete only the old token
        tokenRepository.delete(existingToken);
        final Token savedRefreshToken = tokenRepository.save(newRefreshToken);

        user.setRefreshToken(savedRefreshToken);
        user.setAccessToken(accessToken);
        return user;
    }

    /**
     * Generate a token which can be used for 24 hours.
     */
    public Token generateToken(final User user, final TokenType type) {
        tokenRepository.invalidateTokensWithTypeForUser(List.of(type), user);
        final Token token = Token.builder()
            .value(UUID.randomUUID().toString())
            .type(type)
            .expiresAt(now().plusDays(1))
            .user(user)
            .build();
        log.info("Generated '{}' token for user '{}'", type, user.getEmail());
        return tokenRepository.save(token);
    }

    /**
     * Delete a token by its id, no-op if not found.
     *
     * @param id the token id
     */
    public void deleteById(final Long id) {
        tokenRepository.findById(id).ifPresent(tokenRepository::delete);
    }

    /**
     * Invalidate all tokens of the provided types for the specified user.
     */
    public void invalidateTokensForUser(final User user, final TokenType... types) {
        log.info("Invalidating '{}' token for user '{}'", types, user.getEmail());
        tokenRepository.invalidateTokensWithTypeForUser(List.of(types), user);
    }

    /**
     * Check if the token is a valid access token for the given user.
     */
    public boolean isValidAccessToken(final String jwt, final User user) {
        return jwtService.isTokenValid(jwt, user);
    }

    /**
     * Returns token details for the given token value if it exists.
     */
    public Token findAuthorizationTokenByValue(final String token) {
        log.info("Looking up authorization token by value");
        return tokenRepository.findByValue(token)
            .filter(entry -> entry.getType().isAccessToken() || entry.getType().isRefreshToken())
            .orElse(null);
    }

    /**
     * Returns token details for the given token value if it exists.
     */
    public Token findValidationTokenByValue(final String token) {
        log.info("Looking up validation token by value");
        return tokenRepository.findByValue(token)
            .filter(entry -> entry.getType().equals(USER_VALIDATION_TOKEN))
            .orElseThrow(() -> new InvalidTokenException(TOKEN_NOT_FOUND_ERROR));
    }


    /**
     * Find a password reset token by its value.
     */
    public Token findPasswordResetTokenByValue(final String token) {
        log.info("Looking up password reset token by value");
        return tokenRepository.findByValue(token)
            .filter(entry -> entry.getType().equals(RESET_PASSWORD_TOKEN))
            .orElseThrow(() -> new InvalidTokenException(TOKEN_NOT_FOUND_ERROR));
    }
}
