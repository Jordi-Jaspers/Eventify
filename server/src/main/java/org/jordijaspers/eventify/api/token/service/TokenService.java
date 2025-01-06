package org.jordijaspers.eventify.api.token.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hawaiiframework.repository.DataNotFoundException;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.token.model.TokenType;
import org.jordijaspers.eventify.api.token.repository.TokenRepository;
import org.jordijaspers.eventify.api.user.model.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static org.jordijaspers.eventify.api.token.model.TokenType.*;
import static org.jordijaspers.eventify.common.constants.Constants.Time.MILLIS_PER_SECOND;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.TOKEN_NOT_FOUND_ERROR;

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
     * Remove all expired tokens from the database.
     */
    @Scheduled(fixedDelay = 30 * MILLIS_PER_SECOND)
    public void deleteExpiredTokens() {
        tokenRepository.deleteExpiredTokens();
    }

    /**
     * Generate an access token for a user. The access token is valid for 15 minutes.
     */
    public User generateAuthorizationTokens(final User user) {
        tokenRepository.invalidateTokensWithTypeForUser(List.of(REFRESH_TOKEN, ACCESS_TOKEN), user);
        final Token accessToken = jwtService.generateAccessToken(user);
        final Token refreshToken = tokenRepository.save(jwtService.generateRefreshToken(user));
        log.info("Generated Access & Refresh tokens for user '{}'", user.getEmail());
        user.setRefreshToken(refreshToken);
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
            .expiresAt(LocalDateTime.now().plusDays(1))
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
        final Token entry = tokenRepository.findByValue(jwt).orElse(null);
        return nonNull(entry)
            && jwtService.isTokenValid(entry.getValue(), user)
            && entry.getType().isAccessToken();
    }

    /**
     * Check if the token is a valid refresh token for the given user.
     */
    public boolean isValidToken(final String jwt, final User user) {
        final Token entry = tokenRepository.findByValue(jwt).orElse(null);
        return nonNull(entry)
            && jwtService.isTokenValid(entry.getValue(), user)
            && (entry.getType().isRefreshToken() || entry.getType().isAccessToken());
    }

    /**
     * Returns token details for the given token value if it exists.
     */
    public Token findValidationTokenByValue(final String token) {
        log.info("Searching validation token with value '{}'", token);
        return tokenRepository.findByValue(token)
            .filter(entry -> entry.getType().equals(USER_VALIDATION_TOKEN))
            .orElseThrow(() -> new DataNotFoundException(TOKEN_NOT_FOUND_ERROR));
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
     * Find a password reset token by its value.
     */
    public Token findPasswordResetTokenByValue(final String token) {
        log.info("Searching token with value '{}'", token);
        return tokenRepository.findByValue(token)
            .filter(entry -> entry.getType().equals(RESET_PASSWORD_TOKEN))
            .orElseThrow(() -> new DataNotFoundException(TOKEN_NOT_FOUND_ERROR));
    }
}
