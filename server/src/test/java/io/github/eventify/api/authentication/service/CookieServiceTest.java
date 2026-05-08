package io.github.eventify.api.authentication.service;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.token.model.TokenType;
import io.github.eventify.common.config.properties.SecurityProperties;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static io.github.eventify.common.constant.Constants.Security.ACCESS_TOKEN_COOKIE;
import static io.github.eventify.common.constant.Constants.Security.DEVICE_ID_COOKIE;
import static io.github.eventify.common.constant.Constants.Security.REFRESH_TOKEN_COOKIE;
import static java.time.ZoneOffset.UTC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - CookieService")
public class CookieServiceTest extends UnitTest {

    @Mock
    private SecurityProperties securityProperties;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

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
    @DisplayName("Should set Secure=true on clear cookies when secureCookies is enabled and NOT clear device cookie")
    public void shouldSetSecureFlagOnClearCookiesWhenEnabled() {
        // Given: Security properties with secure cookies enabled
        when(securityProperties.isSecureCookies()).thenReturn(true);

        // When: Clearing auth cookies
        final ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        cookieService.clearAuthCookies(response);

        // Then: Cleared cookies should have Secure=true
        verify(response, atLeastOnce()).addCookie(cookieCaptor.capture());
        final java.util.List<Cookie> capturedCookies = cookieCaptor.getAllValues();

        assertThat(
            "All cleared cookies should have Secure=true",
            capturedCookies.stream().allMatch(Cookie::getSecure),
            is(true)
        );

        // And: The device cookie should NOT be among the cleared cookies
        assertThat(
            "Device cookie must NOT be cleared on logout",
            capturedCookies.stream().noneMatch(c -> DEVICE_ID_COOKIE.equals(c.getName())),
            is(true)
        );

        // And: Only access and refresh token cookies should be cleared
        assertThat(capturedCookies.stream().anyMatch(c -> ACCESS_TOKEN_COOKIE.equals(c.getName())), is(true));
        assertThat(capturedCookies.stream().anyMatch(c -> REFRESH_TOKEN_COOKIE.equals(c.getName())), is(true));
    }

    @Test
    @DisplayName("Should set Secure=false on clear cookies when secureCookies is disabled and NOT clear device cookie")
    public void shouldNotSetSecureFlagOnClearCookiesWhenDisabled() {
        // Given: Security properties with secure cookies disabled
        when(securityProperties.isSecureCookies()).thenReturn(false);

        // When: Clearing auth cookies
        final ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        cookieService.clearAuthCookies(response);

        // Then: Cleared cookies should have Secure=false
        verify(response, atLeastOnce()).addCookie(cookieCaptor.capture());
        final java.util.List<Cookie> capturedCookies = cookieCaptor.getAllValues();

        assertThat(
            "All cleared cookies should have Secure=false",
            capturedCookies.stream().noneMatch(Cookie::getSecure),
            is(true)
        );

        // And: The device cookie should NOT be among the cleared cookies
        assertThat(
            "Device cookie must NOT be cleared on logout",
            capturedCookies.stream().noneMatch(c -> DEVICE_ID_COOKIE.equals(c.getName())),
            is(true)
        );
    }

    @Test
    @DisplayName("Should set device cookie with 10-year max-age, HttpOnly, and correct name")
    public void shouldSetDeviceCookieWithCorrectAttributes() {
        // Given: Security properties with secure cookies enabled
        when(securityProperties.isSecureCookies()).thenReturn(true);

        // And: A device UUID
        final UUID deviceId = UUID.randomUUID();

        // When: Setting the device cookie
        final ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        cookieService.setDeviceCookie(response, deviceId);

        // Then: The device cookie should be set with correct attributes
        verify(response).addCookie(cookieCaptor.capture());
        final Cookie deviceCookie = cookieCaptor.getValue();

        assertThat(deviceCookie.getName(), is(equalTo(DEVICE_ID_COOKIE)));
        assertThat(deviceCookie.getValue(), is(equalTo(deviceId.toString())));
        assertThat(deviceCookie.isHttpOnly(), is(true));
        assertThat(deviceCookie.getSecure(), is(true));

        // And: Max-age should be approximately 10 years (315360000 seconds)
        final int tenYearsInSeconds = 60 * 60 * 24 * 365 * 10;
        assertThat(deviceCookie.getMaxAge() >= tenYearsInSeconds - 60, is(true));
    }

    @Test
    @DisplayName("Should read device ID from cookie when present")
    public void shouldReadDeviceIdWhenCookieIsPresent() {
        // Given: A request with a device cookie
        final UUID deviceId = UUID.randomUUID();
        when(request.getCookies()).thenReturn(
            new Cookie[] {
                new Cookie(DEVICE_ID_COOKIE, deviceId.toString())
            }
        );

        // When: Reading the device ID
        final Optional<UUID> result = cookieService.readDeviceId(request);

        // Then: The device ID should be present and match
        assertThat(result.isPresent(), is(true));
        assertThat(result.get(), is(equalTo(deviceId)));
    }

    @Test
    @DisplayName("Should return empty when device cookie is absent")
    public void shouldReturnEmptyWhenDeviceCookieIsAbsent() {
        // Given: A request with no cookies
        when(request.getCookies()).thenReturn(null);

        // When: Reading the device ID
        final Optional<UUID> result = cookieService.readDeviceId(request);

        // Then: The result should be empty
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("Should generate and set a new device ID when cookie is missing")
    public void shouldGenerateDeviceIdWhenCookieIsMissing() {
        // Given: A request with no device cookie
        when(request.getCookies()).thenReturn(null);
        when(securityProperties.isSecureCookies()).thenReturn(false);

        // When: Ensuring device ID
        final UUID result = cookieService.ensureDeviceId(request, response);

        // Then: A new UUID should be returned
        assertThat(result, is(notNullValue()));

        // And: A new device cookie should be set on the response
        final ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        assertThat(cookieCaptor.getValue().getName(), is(equalTo(DEVICE_ID_COOKIE)));
        assertThat(cookieCaptor.getValue().getValue(), is(equalTo(result.toString())));
    }

    @Test
    @DisplayName("Should return existing device ID without setting new cookie when cookie is present")
    public void shouldReturnExistingDeviceIdWhenCookieIsPresent() {
        // Given: A request with an existing device cookie
        final UUID existingDeviceId = UUID.randomUUID();
        when(request.getCookies()).thenReturn(
            new Cookie[] {
                new Cookie(DEVICE_ID_COOKIE, existingDeviceId.toString())
            }
        );

        // When: Ensuring device ID
        final UUID result = cookieService.ensureDeviceId(request, response);

        // Then: The existing device ID should be returned
        assertThat(result, is(equalTo(existingDeviceId)));

        // And: No new cookie should be set
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("Should return empty when device cookie has malformed UUID")
    public void shouldReturnEmptyWhenDeviceCookieHasMalformedUuid() {
        // Given: A request with a device cookie containing a malformed UUID value
        when(request.getCookies()).thenReturn(
            new Cookie[] {
                new Cookie(DEVICE_ID_COOKIE, "not-a-uuid")
            }
        );

        // When: Reading the device ID
        final Optional<UUID> result = cookieService.readDeviceId(request);

        // Then: The result should be empty (malformed UUID is silently ignored)
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("Should generate a new UUID and set cookie when device cookie has malformed UUID")
    public void ensureDeviceIdShouldGenerateNewIdWhenCookieHasMalformedUuid() {
        // Given: A request with a device cookie containing a malformed UUID value
        when(request.getCookies()).thenReturn(
            new Cookie[] {
                new Cookie(DEVICE_ID_COOKIE, "not-a-uuid")
            }
        );
        when(securityProperties.isSecureCookies()).thenReturn(false);

        // When: Ensuring device ID
        final UUID result = cookieService.ensureDeviceId(request, response);

        // Then: A new valid UUID should be returned
        assertThat(result, is(notNullValue()));

        // And: A new device cookie should be set on the response with the generated UUID
        final ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        assertThat(cookieCaptor.getValue().getName(), is(equalTo(DEVICE_ID_COOKIE)));
        assertThat(cookieCaptor.getValue().getValue(), is(equalTo(result.toString())));
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
