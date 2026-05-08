package io.github.eventify.api.authentication.service;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.common.config.properties.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import static io.github.eventify.common.constant.Constants.Security.ACCESS_TOKEN_COOKIE;
import static io.github.eventify.common.constant.Constants.Security.DEVICE_ID_COOKIE;
import static io.github.eventify.common.constant.Constants.Security.REFRESH_TOKEN_COOKIE;
import static io.github.eventify.common.util.TimeProvider.now;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * A service to manage cookies.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CookieService {

    private static final String COOKIE_PATH = "/";
    private static final String SAME_SITE_ATTRIBUTE = "SameSite";
    private static final String SAME_SITE_VALUE = "Lax";
    private static final int TEN_YEARS_IN_SECONDS = 60 * 60 * 24 * 365 * 10;

    private final SecurityProperties securityProperties;

    /**
     * Sets the access and refresh token as cookies in the response.
     *
     * @param response     the response to set the cookies in
     * @param accessToken  the access token
     * @param refreshToken the refresh token
     */
    public void setAuthCookies(final HttpServletResponse response, final Token accessToken, final Token refreshToken) {
        setAccessTokenCookie(response, accessToken);
        setRefreshTokenCookie(response, refreshToken);
    }

    /**
     * Sets the access token as a cookie in the response.
     *
     * @param response    the response to set the cookie in
     * @param accessToken the access token
     */
    public void setAccessTokenCookie(final HttpServletResponse response, final Token accessToken) {
        log.debug("Setting access token cookie");
        response.addCookie(createCookie(ACCESS_TOKEN_COOKIE, accessToken.getRawValue(), accessToken.getExpiresAt()));
    }

    /**
     * Sets the refresh token as a cookie in the response.
     *
     * @param response     the response to set the cookie in
     * @param refreshToken the refresh token
     */
    public void setRefreshTokenCookie(final HttpServletResponse response, final Token refreshToken) {
        log.debug("Setting refresh token cookie");
        response.addCookie(createCookie(REFRESH_TOKEN_COOKIE, refreshToken.getRawValue(), refreshToken.getExpiresAt()));
    }

    /**
     * Clears the access and refresh token cookies in the response. Does NOT clear the device cookie.
     *
     * @param response the response to clear the cookies in
     */
    public void clearAuthCookies(final HttpServletResponse response) {
        final OffsetDateTime expiredYesterday = now().minusDays(1);
        response.addCookie(createCookie(ACCESS_TOKEN_COOKIE, "", expiredYesterday));
        response.addCookie(createCookie(REFRESH_TOKEN_COOKIE, "", expiredYesterday));
    }

    /**
     * Sets the device identity cookie with a 10-year expiry.
     *
     * @param response the response to set the cookie in
     * @param deviceId the device UUID
     */
    public void setDeviceCookie(final HttpServletResponse response, final UUID deviceId) {
        log.debug("Setting device cookie");
        final Cookie cookie = new Cookie(DEVICE_ID_COOKIE, deviceId.toString());
        cookie.setHttpOnly(true);
        cookie.setSecure(securityProperties.isSecureCookies());
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge(TEN_YEARS_IN_SECONDS);
        cookie.setAttribute(SAME_SITE_ATTRIBUTE, SAME_SITE_VALUE);
        response.addCookie(cookie);
    }

    /**
     * Reads the device ID from the request cookies.
     *
     * @param request the HTTP request
     * @return an {@link Optional} containing the device UUID, or empty if not present or invalid
     */
    public Optional<UUID> readDeviceId(final HttpServletRequest request) {
        return readCookieValue(request, DEVICE_ID_COOKIE)
            .flatMap(value -> {
                try {
                    return Optional.of(UUID.fromString(value));
                } catch (final IllegalArgumentException ex) {
                    log.warn("Invalid device cookie value: {}", value);
                    return Optional.empty();
                }
            });
    }

    /**
     * Returns the existing device ID from the cookie, or generates a new one and sets it on the response.
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @return the existing or newly generated device UUID
     */
    public UUID ensureDeviceId(final HttpServletRequest request, final HttpServletResponse response) {
        final Optional<UUID> existing = readDeviceId(request);
        if (existing.isPresent()) {
            return existing.get();
        }
        final UUID newDeviceId = UUID.randomUUID();
        setDeviceCookie(response, newDeviceId);
        return newDeviceId;
    }

    /**
     * Reads the refresh token value from the request cookies.
     *
     * @param request the HTTP request
     * @return an {@link Optional} containing the refresh token value, or empty if not present
     */
    public Optional<String> readRefreshTokenValue(final HttpServletRequest request) {
        return readCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    private Optional<String> readCookieValue(final HttpServletRequest request, final String cookieName) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
            .filter(c -> cookieName.equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst();
    }

    private Cookie createCookie(final String name, final String value, final OffsetDateTime expiresAt) {
        final Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(securityProperties.isSecureCookies());
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge((int) now().until(expiresAt, SECONDS));
        cookie.setAttribute(SAME_SITE_ATTRIBUTE, SAME_SITE_VALUE);
        return cookie;
    }

}
