package io.github.eventify.api.token.service;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.config.properties.SecurityProperties;
import io.github.eventify.common.config.properties.TokenProperties;
import io.github.eventify.common.exception.InvalidJwtException;
import io.github.jframe.autoconfigure.properties.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import static io.github.eventify.api.token.model.JWTClaimNames.*;
import static io.github.eventify.api.token.model.TokenType.ACCESS_TOKEN;
import static io.github.eventify.api.token.model.TokenType.REFRESH_TOKEN;
import static java.time.ZoneOffset.UTC;

/**
 * The service to extract data from a valid JWT token.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final ApplicationProperties applicationProperties;

    private final SecurityProperties securityProperties;

    private final JwtEncoder encoder;

    private final JwtDecoder decoder;

    // ================================ Token Generation ================================

    /**
     * Generate a JWT access token for the user with the given claims.
     */
    public <T extends UserDetails> Token generateAccessToken(final T user) {
        final OffsetDateTime now = OffsetDateTime.now(UTC);
        final User userDetails = (User) user;
        final String[] permissions = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toArray(String[]::new);

        final TokenProperties properties = securityProperties.getAccessToken();
        final JwtClaimsSet claimsSet = JwtClaimsSet.builder()
            .id(UUID.randomUUID().toString())
            .subject(user.getUsername())
            .issuer(applicationProperties.getUrl())
            .issuedAt(now.toInstant())
            .audience(List.of(applicationProperties.getUrl()))
            .expiresAt(now.plus(properties.getLifetime(), properties.getTimeUnit()).toInstant())
            .claim(EMAIL, userDetails.getEmail())
            .claim(FIRST_NAME, userDetails.getFirstName())
            .claim(LAST_NAME, userDetails.getLastName())
            .claim(LAST_LOGIN, userDetails.getLastLogin().toEpochSecond())
            .claim(CREATED, userDetails.getCreatedAt().toEpochSecond())
            .claim(GLOBAL_ROLE, userDetails.getRole())
            .claim(PERMISSIONS, permissions)
            .claim(ENABLED, userDetails.isEnabled())
            .claim(VALIDATED, userDetails.isValidated())
            .build();

        return new Token(
            encoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue(),
            OffsetDateTime.ofInstant(claimsSet.getExpiresAt(), UTC),
            ACCESS_TOKEN,
            (User) user
        );
    }

    /**
     * Generate a Refresh JWT token for a user with the given claims.
     */
    public <T extends UserDetails> Token generateRefreshToken(final T user) {
        final OffsetDateTime now = OffsetDateTime.now(UTC);
        final TokenProperties properties = securityProperties.getRefreshToken();
        final JwtClaimsSet claimsSet = JwtClaimsSet.builder()
            .id(UUID.randomUUID().toString())
            .subject(user.getUsername())
            .issuer(applicationProperties.getUrl())
            .issuedAt(now.toInstant())
            .audience(List.of(applicationProperties.getUrl()))
            .expiresAt(now.plus(properties.getLifetime(), properties.getTimeUnit()).toInstant())
            .build();

        return new Token(
            encoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue(),
            OffsetDateTime.ofInstant(claimsSet.getExpiresAt(), UTC),
            REFRESH_TOKEN,
            (User) user
        );
    }

    // ================================ Token Extraction ================================

    /**
     * Extract the user from the JWT token.
     */
    public String extractSubject(final String token) {
        return toJwt(token).getSubject();
    }

    /**
     * Extract the expiration date from the JWT token.
     */
    public Instant extractExpiration(final String token) {
        return toJwt(token).getExpiresAt();
    }

    /**
     * transforms the jwt token string to a Jwt object.
     */
    private Jwt toJwt(final String token) {
        try {
            return decoder.decode(token);
        } catch (final Exception exception) {
            log.error(exception.getMessage());
            throw new InvalidJwtException(exception);
        }
    }

    // ================================ Token Validation ================================

    /**
     * Check if the token is valid for the given user and not expired.
     */
    public <T extends UserDetails> boolean isTokenValid(final String token, final T principal) {
        final String email = extractSubject(token);
        return principal.getUsername().equalsIgnoreCase(email)
            && !isTokenExpired(token);
    }

    /**
     * Check if the token is expired.
     */
    public boolean isTokenExpired(final String jwt) {
        final Instant expirationDate = extractExpiration(jwt);
        return expirationDate.isBefore(Instant.now());
    }
}
