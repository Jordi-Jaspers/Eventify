package io.github.eventify.api.token.service;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.token.repository.TokenRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.InvalidJwtException;
import io.github.eventify.common.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.api.token.model.TokenType.*;
import static io.github.eventify.common.exception.ApiErrorCode.TOKEN_NOT_FOUND_ERROR;
import static java.time.ZoneOffset.UTC;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MINUTES;

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
     * Remove all expired tokens from the database. This method runs every 5 minutes to clean up expired tokens
     * in a single batch operation. The operation runs in a new transaction to avoid impacting other database operations.
     */
    @Scheduled(
        fixedDelay = 5,
        timeUnit = MINUTES
    )
    public void deleteExpiredTokens() {
        final int deletedTokens = tokenRepository.deleteExpiredTokens();
        log.debug("Successfully deleted '{}' expired tokens.", deletedTokens);
    }

    /**
     * Generate an access token for a user. The access token is valid for 15 minutes.
     */
    public User generateAuthorizationTokens(final User user) {
        invalidateTokensForUser(user, ACCESS_TOKEN, REFRESH_TOKEN);

        final Token accessToken = jwtService.generateAccessToken(user);
        final Token refreshToken = tokenRepository.save(jwtService.generateRefreshToken(user));
        log.info("Generated Access & Refresh tokens for user '{}'", user.getEmail());

        user.setRefreshToken(refreshToken);
        user.setAccessToken(accessToken);
        return user;
    }

    /**
     * Refreshes the access token using the refresh token.
     *
     * @param refreshToken the refresh token
     * @return the user with refreshed tokens
     */
    public User refresh(final String refreshToken) {
        final Token token = findAuthorizationTokenByValue(refreshToken);
        if (nonNull(token)) {
            final User user = token.getUser();
            log.info("Refreshing tokens for user '{}'", user.getUsername());
            return generateAuthorizationTokens(user);
        } else {
            throw new InvalidJwtException();
        }
    }

    /**
     * Generate a token which can be used for 24 hours.
     */
    public Token generateToken(final User user, final TokenType type) {
        tokenRepository.invalidateTokensWithTypeForUser(List.of(type), user);
        final Token token = Token.builder()
            .value(UUID.randomUUID().toString())
            .type(type)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(1))
            .user(user)
            .build();
        log.info("Generated '{}' token for user '{}'", type, user.getEmail());
        return tokenRepository.save(token);
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
        log.info("Searching jwt token with value '{}'", token);
        return tokenRepository.findByValue(token)
            .filter(entry -> entry.getType().isAccessToken() || entry.getType().isRefreshToken())
            .orElse(null);
    }

    /**
     * Returns token details for the given token value if it exists.
     */
    public Token findValidationTokenByValue(final String token) {
        log.info("Searching validation token with value '{}'", token);
        return tokenRepository.findByValue(token)
            .filter(entry -> entry.getType().equals(USER_VALIDATION_TOKEN))
            .orElseThrow(() -> new InvalidTokenException(TOKEN_NOT_FOUND_ERROR));
    }


    /**
     * Find a password reset token by its value.
     */
    public Token findPasswordResetTokenByValue(final String token) {
        log.info("Searching token with value '{}'", token);
        return tokenRepository.findByValue(token)
            .filter(entry -> entry.getType().equals(RESET_PASSWORD_TOKEN))
            .orElseThrow(() -> new InvalidTokenException(TOKEN_NOT_FOUND_ERROR));
    }
}
