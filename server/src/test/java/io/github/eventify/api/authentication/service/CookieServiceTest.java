package io.github.eventify.api.authentication.service;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.common.config.properties.SecurityProperties;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static io.github.eventify.common.constant.Constants.Security.ACCESS_TOKEN_COOKIE;
import static io.github.eventify.common.constant.Constants.Security.REFRESH_TOKEN_COOKIE;
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - CookieService")
public class CookieServiceTest extends UnitTest {

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private HttpServletResponse response;

    private CookieService cookieService;

    @BeforeEach
    public void setUp() {
        cookieService = new CookieService(securityProperties);
    }

    @Test
    @DisplayName("Should set Secure=true on auth cookies when secureCookies is enabled")
    public void shouldSetSecureFlagWhenSecureCookiesEnabled() {
        // Given: Security properties with secure cookies enabled
        when(securityProperties.isSecureCookies()).thenReturn(true);

        // And: Valid access and refresh tokens
        final Token accessToken = aToken(ACCESS_TOKEN_COOKIE, TokenType.ACCESS_TOKEN, 1);
        final Token refreshToken = aToken(REFRESH_TOKEN_COOKIE, TokenType.REFRESH_TOKEN, 30);

        // When: Setting auth cookies
        final ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        cookieService.setAuthCookies(response, accessToken, refreshToken);

        // Then: Both cookies should have Secure=true
        verify(response, atLeastOnce()).addCookie(cookieCaptor.capture());
        final java.util.List<Cookie> capturedCookies = cookieCaptor.getAllValues();

        assertThat(
            "All cookies should have Secure=true",
            capturedCookies.stream().allMatch(Cookie::getSecure),
            is(true)
        );
    }

    @Test
    @DisplayName("Should set Secure=false on auth cookies when secureCookies is disabled")
    public void shouldNotSetSecureFlagWhenSecureCookiesDisabled() {
        // Given: Security properties with secure cookies disabled
        when(securityProperties.isSecureCookies()).thenReturn(false);

        // And: Valid access and refresh tokens
        final Token accessToken = aToken(ACCESS_TOKEN_COOKIE, TokenType.ACCESS_TOKEN, 1);
        final Token refreshToken = aToken(REFRESH_TOKEN_COOKIE, TokenType.REFRESH_TOKEN, 30);

        // When: Setting auth cookies
        final ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        cookieService.setAuthCookies(response, accessToken, refreshToken);

        // Then: Both cookies should have Secure=false
        verify(response, atLeastOnce()).addCookie(cookieCaptor.capture());
        final java.util.List<Cookie> capturedCookies = cookieCaptor.getAllValues();

        assertThat(
            "All cookies should have Secure=false",
            capturedCookies.stream().noneMatch(Cookie::getSecure),
            is(true)
        );
    }

    @Test
    @DisplayName("Should set Secure=true on access token cookie when secureCookies is enabled")
    public void shouldSetSecureFlagOnAccessTokenCookieWhenEnabled() {
        // Given: Security properties with secure cookies enabled
        when(securityProperties.isSecureCookies()).thenReturn(true);

        // And: A valid access token
        final Token accessToken = aToken(ACCESS_TOKEN_COOKIE, TokenType.ACCESS_TOKEN, 1);

        // When: Setting only the access token cookie
        final ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        cookieService.setAccessTokenCookie(response, accessToken);

        // Then: The access token cookie should have Secure=true
        verify(response).addCookie(cookieCaptor.capture());
        assertThat(cookieCaptor.getValue().getSecure(), is(true));
    }

    @Test
    @DisplayName("Should set Secure=false on access token cookie when secureCookies is disabled")
    public void shouldNotSetSecureFlagOnAccessTokenCookieWhenDisabled() {
        // Given: Security properties with secure cookies disabled
        when(securityProperties.isSecureCookies()).thenReturn(false);

        // And: A valid access token
        final Token accessToken = aToken(ACCESS_TOKEN_COOKIE, TokenType.ACCESS_TOKEN, 1);

        // When: Setting only the access token cookie
        final ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        cookieService.setAccessTokenCookie(response, accessToken);

        // Then: The access token cookie should have Secure=false
        verify(response).addCookie(cookieCaptor.capture());
        assertThat(cookieCaptor.getValue().getSecure(), is(false));
    }

    @Test
    @DisplayName("Should set Secure=true on refresh token cookie when secureCookies is enabled")
    public void shouldSetSecureFlagOnRefreshTokenCookieWhenEnabled() {
        // Given: Security properties with secure cookies enabled
        when(securityProperties.isSecureCookies()).thenReturn(true);

        // And: A valid refresh token
        final Token refreshToken = aToken(REFRESH_TOKEN_COOKIE, TokenType.REFRESH_TOKEN, 30);

        // When: Setting only the refresh token cookie
        final ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        cookieService.setRefreshTokenCookie(response, refreshToken);

        // Then: The refresh token cookie should have Secure=true
        verify(response).addCookie(cookieCaptor.capture());
        assertThat(cookieCaptor.getValue().getSecure(), is(true));
    }

    @Test
    @DisplayName("Should set Secure=false on refresh token cookie when secureCookies is disabled")
    public void shouldNotSetSecureFlagOnRefreshTokenCookieWhenDisabled() {
        // Given: Security properties with secure cookies disabled
        when(securityProperties.isSecureCookies()).thenReturn(false);

        // And: A valid refresh token
        final Token refreshToken = aToken(REFRESH_TOKEN_COOKIE, TokenType.REFRESH_TOKEN, 30);

        // When: Setting only the refresh token cookie
        final ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        cookieService.setRefreshTokenCookie(response, refreshToken);

        // Then: The refresh token cookie should have Secure=false
        verify(response).addCookie(cookieCaptor.capture());
        assertThat(cookieCaptor.getValue().getSecure(), is(false));
    }

    @Test
    @DisplayName("Should set Secure=true on clear cookies when secureCookies is enabled")
    public void shouldSetSecureFlagOnClearCookiesWhenEnabled() {
        // Given: Security properties with secure cookies enabled
        when(securityProperties.isSecureCookies()).thenReturn(true);

        // When: Clearing auth cookies
        final ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        cookieService.clearAuthCookies(response);

        // Then: Both cleared cookies should have Secure=true
        verify(response, atLeastOnce()).addCookie(cookieCaptor.capture());
        final java.util.List<Cookie> capturedCookies = cookieCaptor.getAllValues();

        assertThat(
            "All cleared cookies should have Secure=true",
            capturedCookies.stream().allMatch(Cookie::getSecure),
            is(true)
        );
    }

    @Test
    @DisplayName("Should set Secure=false on clear cookies when secureCookies is disabled")
    public void shouldNotSetSecureFlagOnClearCookiesWhenDisabled() {
        // Given: Security properties with secure cookies disabled
        when(securityProperties.isSecureCookies()).thenReturn(false);

        // When: Clearing auth cookies
        final ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        cookieService.clearAuthCookies(response);

        // Then: Both cleared cookies should have Secure=false
        verify(response, atLeastOnce()).addCookie(cookieCaptor.capture());
        final java.util.List<Cookie> capturedCookies = cookieCaptor.getAllValues();

        assertThat(
            "All cleared cookies should have Secure=false",
            capturedCookies.stream().noneMatch(Cookie::getSecure),
            is(true)
        );
    }

    // ========================= FACTORY METHODS =========================

    private static Token aToken(final String name, final TokenType type, final int expiryDays) {
        return Token.builder()
            .value(name + "-value")
            .type(type)
            .expiresAt(OffsetDateTime.now(UTC).plusDays(expiryDays))
            .build();
    }
}
