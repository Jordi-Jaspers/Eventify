package io.github.eventify.api.token.service;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.api.token.repository.TokenRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.InvalidJwtException;
import io.github.eventify.common.exception.InvalidTokenException;
import io.github.eventify.common.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.api.token.model.TokenType.RESET_PASSWORD_TOKEN;
import static io.github.eventify.api.token.model.TokenType.USER_VALIDATION_TOKEN;
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
     * Generate authorization tokens for a user, capturing device info from the request. Uses the familyId to find and reuse an existing
     * session row (same-session-replace), or creates a new one.
     *
     * @param user       the user to generate tokens for
     * @param request    the HTTP request to extract device info from (may be {@code null})
     * @param rememberMe if {@code true}, the refresh token uses the remember-me lifetime
     * @param familyId   the device/session family identifier (from persistent device cookie)
     * @return the user with new tokens set
     */
    public User generateAuthorizationTokens(final User user, final HttpServletRequest request, final boolean rememberMe,
        final UUID familyId) {

        final List<Token> existingSessions = tokenRepository.findByEmail(user.getEmail());
        log.debug(
            "Creating/updating session for '{}' of familyId '{}'; {} existing session(s)",
            user.getEmail(),
            familyId,
            existingSessions.size()
        );

        final Token accessToken = jwtService.generateAccessToken(user);
        final Token newRefreshToken = jwtService.generateRefreshToken(user, rememberMe);

        final String rawRefreshValue = newRefreshToken.getRawValue();
        final String refreshHash = HashUtil.sha256(rawRefreshValue);

        final Token savedRefreshToken = tokenRepository.save(
            upsertRefreshToken(newRefreshToken, rawRefreshValue, refreshHash, familyId, user, request)
        );
        savedRefreshToken.setRawValue(rawRefreshValue);
        log.info("Generated Access & Refresh tokens for user '{}'", user.getEmail());

        user.setRefreshToken(savedRefreshToken);
        user.setAccessToken(accessToken);
        return user;
    }

    /**
     * Convenience overload — generates tokens with a random familyId (for callers that don't manage device cookies).
     */
    public User generateAuthorizationTokens(final User user, final HttpServletRequest request, final boolean rememberMe) {
        return generateAuthorizationTokens(user, request, rememberMe, UUID.randomUUID());
    }

    /**
     * Convenience overload — generates tokens with rememberMe=false and a random familyId.
     */
    public User generateAuthorizationTokens(final User user, final HttpServletRequest request) {
        return generateAuthorizationTokens(user, request, false, UUID.randomUUID());
    }

    /**
     * Refreshes the access token using the refresh token, preserving device info on the rotated token.
     *
     * @param refreshToken the raw refresh token value
     * @param request      the HTTP request (used to update {@code lastActiveAt})
     * @return the user with refreshed tokens
     */
    public User refresh(final String refreshToken, final HttpServletRequest request) {
        final String hash = HashUtil.sha256(refreshToken);
        final Token existingToken = tokenRepository.findByValueHash(hash)
            .filter(t -> t.getType().isAccessToken() || t.getType().isRefreshToken())
            .orElse(null);

        if (existingToken == null) {
            throw new InvalidJwtException();
        }

        final User user = existingToken.getUser();
        log.info("Refreshing tokens for user '{}'", user.getUsername());

        final Token accessToken = jwtService.generateAccessToken(user);
        // Intentional MVP downgrade: rotated refresh tokens never inherit remember-me lifetime.
        final Token newRefreshToken = jwtService.generateRefreshToken(user, false);
        newRefreshToken.inheritDeviceMetadataFrom(existingToken);
        newRefreshToken.setLastActiveAt(now());

        final String newRawValue = newRefreshToken.getRawValue();
        final String newHash = HashUtil.sha256(newRawValue);
        newRefreshToken.setValueHash(newHash);
        newRefreshToken.setFamilyId(existingToken.getFamilyId());

        // Delete only the old token
        tokenRepository.delete(existingToken);
        final Token savedRefreshToken = tokenRepository.save(newRefreshToken);
        savedRefreshToken.setRawValue(newRawValue);

        user.setRefreshToken(savedRefreshToken);
        user.setAccessToken(accessToken);
        return user;
    }

    /**
     * Generate a token which can be used for 24 hours. For non-refresh tokens, the raw value is stored directly in valueHash (not hashed).
     */
    public Token generateToken(final User user, final TokenType type) {
        tokenRepository.invalidateTokensWithTypeForUser(List.of(type), user);
        final String rawValue = UUID.randomUUID().toString();
        final Token token = Token.builder()
            .valueHash(rawValue)
            .rawValue(rawValue)
            .familyId(UUID.randomUUID())
            .type(type)
            .expiresAt(now().plusDays(1))
            .user(user)
            .build();
        log.info("Generated '{}' token for user '{}'", type, user.getEmail());
        final Token saved = tokenRepository.save(token);
        saved.setRawValue(rawValue);
        return saved;
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
     * Returns token details for the given raw token value if it exists (access or refresh tokens only).
     */
    public Token findAuthorizationTokenByValue(final String rawToken) {
        log.info("Looking up authorization token by value");
        final String hash = HashUtil.sha256(rawToken);
        return tokenRepository.findByValueHash(hash)
            .filter(entry -> entry.getType().isAccessToken() || entry.getType().isRefreshToken())
            .orElse(null);
    }

    /**
     * Returns token details for the given raw token value if it exists (validation tokens only). Validation tokens store the raw value
     * directly in valueHash (not hashed).
     */
    public Token findValidationTokenByValue(final String token) {
        log.info("Looking up validation token by value");
        return tokenRepository.findByValueHash(token)
            .filter(entry -> entry.getType().equals(USER_VALIDATION_TOKEN))
            .orElseThrow(() -> new InvalidTokenException(TOKEN_NOT_FOUND_ERROR));
    }

    /**
     * Find a password reset token by its value. Password reset tokens store the raw value directly in valueHash (not hashed).
     */
    public Token findPasswordResetTokenByValue(final String token) {
        log.info("Looking up password reset token by value");
        return tokenRepository.findByValueHash(token)
            .filter(entry -> entry.getType().equals(RESET_PASSWORD_TOKEN))
            .orElseThrow(() -> new InvalidTokenException(TOKEN_NOT_FOUND_ERROR));
    }

    private Token upsertRefreshToken(
        final Token newRefreshToken,
        final String rawValue,
        final String hash,
        final UUID familyId,
        final User user,
        final HttpServletRequest request) {
        final UUID resolvedFamilyId = familyId != null ? familyId : UUID.randomUUID();
        final Optional<Token> existingSession = familyId != null
            ? tokenRepository.findByUserIdAndFamilyId(user.getId(), familyId)
            : Optional.empty();

        if (existingSession.isPresent()) {
            final Token existing = existingSession.get();
            existing.setValueHash(hash);
            existing.setRawValue(rawValue);
            existing.setExpiresAt(newRefreshToken.getExpiresAt());
            existing.captureDeviceMetadata(request);
            return existing;
        }
        applyHashAndDevice(newRefreshToken, hash, resolvedFamilyId, request);
        return newRefreshToken;
    }

    private void applyHashAndDevice(
        final Token token,
        final String hash,
        final UUID familyId,
        final HttpServletRequest request) {
        token.setValueHash(hash);
        token.setFamilyId(familyId);
        token.captureDeviceMetadata(request);
    }
}
