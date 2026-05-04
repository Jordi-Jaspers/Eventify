package io.github.eventify.api.authentication.service;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.common.config.properties.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.Optional;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

import static io.github.eventify.common.constant.Constants.Security.ACCESS_TOKEN_COOKIE;
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
        response.addCookie(createCookie(ACCESS_TOKEN_COOKIE, accessToken.getValue(), accessToken.getExpiresAt()));
    }

    /**
     * Sets the refresh token as a cookie in the response.
     *
     * @param response     the response to set the cookie in
     * @param refreshToken the refresh token
     */
    public void setRefreshTokenCookie(final HttpServletResponse response, final Token refreshToken) {
        log.debug("Setting refresh token cookie");
        response.addCookie(createCookie(REFRESH_TOKEN_COOKIE, refreshToken.getValue(), refreshToken.getExpiresAt()));
    }

    /**
     * Clears the access and refresh token cookies in the response.
     *
     * @param response the response to clear the cookies in
     */
    public void clearAuthCookies(final HttpServletResponse response) {
        final OffsetDateTime expiredYesterday = now().minusDays(1);
        response.addCookie(createCookie(ACCESS_TOKEN_COOKIE, "", expiredYesterday));
        response.addCookie(createCookie(REFRESH_TOKEN_COOKIE, "", expiredYesterday));
    }

    /**
     * Reads the refresh token value from the request cookies.
     *
     * @param request the HTTP request
     * @return an {@link Optional} containing the refresh token value, or empty if not present
     */
    public Optional<String> readRefreshTokenValue(final HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return java.util.Arrays.stream(cookies)
            .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
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
