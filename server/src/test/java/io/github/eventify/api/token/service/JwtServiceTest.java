package io.github.eventify.api.token.service;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.config.properties.SecurityProperties;
import io.github.eventify.common.config.properties.TokenProperties;
import io.github.eventify.support.UnitTest;
import io.github.jframe.autoconfigure.properties.ApplicationProperties;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Jwt Service")
public class JwtServiceTest extends UnitTest {

    private static final int SEVEN_DAYS = 7;
    private static final int THIRTY_DAYS = 30;
    private static final int TOLERANCE_SECONDS = 60;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    private JwtService jwtService;

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService(applicationProperties, securityProperties, jwtEncoder, jwtDecoder);

        // Stub application properties
        when(applicationProperties.getUrl()).thenReturn("http://localhost:8080");

        // Stub a mock JwtEncoder that returns a token with a fixed value
        final org.springframework.security.oauth2.jwt.Jwt mockJwt = mock(org.springframework.security.oauth2.jwt.Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn("mock-jwt-token-value");
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);
    }

    @Test
    @DisplayName("Should generate refresh token with 7-day expiry when rememberMe is false")
    public void generateRefreshTokenWithoutRememberMeUsesSevenDayLifetime() {
        // Given: SecurityProperties configured with 7-day refresh token lifetime
        final TokenProperties refreshTokenProps = new TokenProperties();
        refreshTokenProps.setLifetime(SEVEN_DAYS);
        refreshTokenProps.setTimeUnit(ChronoUnit.DAYS);
        when(securityProperties.getRefreshToken()).thenReturn(refreshTokenProps);

        // And: A valid user
        final User user = aValidUser();

        // And: Record the time before generation
        final OffsetDateTime before = OffsetDateTime.now(UTC);

        // When: Generating a refresh token with rememberMe=false
        final Token token = jwtService.generateRefreshToken(user, false);

        // And: Record the time after generation
        final OffsetDateTime after = OffsetDateTime.now(UTC);

        // Then: The token should not be null
        assertThat(token, is(notNullValue()));
        assertThat(token.getExpiresAt(), is(notNullValue()));

        // And: The expiry should be approximately now + 7 days (±60s tolerance)
        final OffsetDateTime expectedMin = before.plusDays(SEVEN_DAYS).minusSeconds(TOLERANCE_SECONDS);
        final OffsetDateTime expectedMax = after.plusDays(SEVEN_DAYS).plusSeconds(TOLERANCE_SECONDS);
        assertThat(token.getExpiresAt().isAfter(expectedMin), is(true));
        assertThat(token.getExpiresAt().isBefore(expectedMax), is(true));
    }

    @Test
    @DisplayName("Should generate refresh token with 30-day expiry when rememberMe is true")
    public void generateRefreshTokenWithRememberMeUsesThirtyDayLifetime() {
        // Given: SecurityProperties configured with 30-day remember-me token lifetime
        final TokenProperties rememberMeTokenProps = new TokenProperties();
        rememberMeTokenProps.setLifetime(THIRTY_DAYS);
        rememberMeTokenProps.setTimeUnit(ChronoUnit.DAYS);
        when(securityProperties.getRememberMeToken()).thenReturn(rememberMeTokenProps);

        // And: A valid user
        final User user = aValidUser();

        // And: Record the time before generation
        final OffsetDateTime before = OffsetDateTime.now(UTC);

        // When: Generating a refresh token with rememberMe=true
        final Token token = jwtService.generateRefreshToken(user, true);

        // And: Record the time after generation
        final OffsetDateTime after = OffsetDateTime.now(UTC);

        // Then: The token should not be null
        assertThat(token, is(notNullValue()));
        assertThat(token.getExpiresAt(), is(notNullValue()));

        // And: The expiry should be approximately now + 30 days (±60s tolerance)
        final OffsetDateTime expectedMin = before.plusDays(THIRTY_DAYS).minusSeconds(TOLERANCE_SECONDS);
        final OffsetDateTime expectedMax = after.plusDays(THIRTY_DAYS).plusSeconds(TOLERANCE_SECONDS);
        assertThat(token.getExpiresAt().isAfter(expectedMin), is(true));
        assertThat(token.getExpiresAt().isBefore(expectedMax), is(true));
    }
}
