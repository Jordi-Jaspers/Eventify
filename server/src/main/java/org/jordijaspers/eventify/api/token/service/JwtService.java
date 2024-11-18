package org.jordijaspers.eventify.api.token.service;

import lombok.RequiredArgsConstructor;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.config.properties.ApplicationProperties;
import org.jordijaspers.eventify.common.exception.InvalidJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static org.jordijaspers.eventify.api.token.model.JWTClaimNames.*;
import static org.jordijaspers.eventify.api.token.model.TokenType.ACCESS_TOKEN;
import static org.jordijaspers.eventify.api.token.model.TokenType.REFRESH_TOKEN;

/**
 * The service to extract data from a valid JWT token.
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtService.class);

    private final ApplicationProperties applicationProperties;

    private final JwtEncoder encoder;

    private final JwtDecoder decoder;

    @Value("${security.jwt.lifetime}")
    private int lifetime;

    // ================================ Token Generation ================================

    /**
     * Generate a JWT access token for a user with the default claims.
     */
    public <T extends UserDetails> Token generateAccessToken(final T user) {
        final String[] roles = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toArray(String[]::new);

        final User userDetails = (User) user;
        final Map<String, Object> claims = new HashMap<>();
        claims.put(AUTHORITIES, roles);
        claims.put(FIRST_NAME, userDetails.getFirstName());
        claims.put(LAST_NAME, userDetails.getLastName());
        claims.put(ENABLED, userDetails.isEnabled());
        claims.put(VALIDATED, userDetails.isValidated());
        claims.put(LAST_LOGIN, ZonedDateTime.of(userDetails.getLastLogin(), UTC));
        claims.put(CREATED, ZonedDateTime.of(userDetails.getCreated(), UTC));
        return generateAccessToken(user, claims);
    }

    /**
     * Generate a JWT access token for the user with the given claims.
     */
    public <T extends UserDetails> Token generateAccessToken(final T user, final Map<String, Object> claims) {
        final LocalDateTime now = LocalDateTime.now();
        final JwtClaimsSet claimsSet = JwtClaimsSet.builder()
            .subject(user.getUsername())
            .issuer(applicationProperties.getUrl())
            .issuedAt(now.toInstant(UTC))
            .audience(List.of(applicationProperties.getUrl()))
            .claims(claims::putAll)
            .expiresAt(now.plusSeconds(lifetime).toInstant(UTC))
            .build();

        return new Token(
            encoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue(),
            now.plusSeconds(lifetime),
            ACCESS_TOKEN,
            (User) user
        );
    }

    /**
     * Generate a Refresh JWT token for a user with the given claims.
     */
    public <T extends UserDetails> Token generateRefreshToken(final T user) {
        final LocalDateTime now = LocalDateTime.now();
        final JwtClaimsSet claimsSet = JwtClaimsSet.builder()
            .subject(user.getUsername())
            .issuer(applicationProperties.getUrl())
            .issuedAt(now.toInstant(UTC))
            .audience(List.of(applicationProperties.getUrl()))
            .expiresAt(now.plusDays(7).toInstant(UTC))
            .build();

        return new Token(
            encoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue(),
            now.plusDays(7),
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
     * Extracts a specific claim from the JWT token.
     */
    private Object extractClaim(final String token, final String claim) {
        return toJwt(token).getClaims().get(claim);
    }

    /**
     * transforms the jwt token string to a Jwt object.
     */
    private Jwt toJwt(final String token) {
        try {
            return decoder.decode(token);
        } catch (final Exception exception) {
            LOGGER.error(exception.getMessage());
            throw new InvalidJwtException(exception);
        }
    }

    // ================================ Token Validation ================================

    /**
     * Check if the token is valid for the given user and not expired.
     */
    public <T extends UserDetails> boolean isTokenValid(final String token, final T principal) {
        final String email = extractSubject(token);
        return principal.getUsername().equalsIgnoreCase(email) && !isTokenExpired(token);
    }

    /**
     * Check if the token is expired.
     */
    public boolean isTokenExpired(final String jwt) {
        final Instant expirationDate = extractExpiration(jwt);
        return expirationDate.isBefore(Instant.now());
    }
}
