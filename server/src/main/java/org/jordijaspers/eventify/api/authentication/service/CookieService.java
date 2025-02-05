package org.jordijaspers.eventify.api.authentication.service;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.jordijaspers.eventify.api.token.model.Token;
import org.springframework.stereotype.Service;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.jordijaspers.eventify.common.constants.Constants.Security.ACCESS_TOKEN_COOKIE;
import static org.jordijaspers.eventify.common.constants.Constants.Security.REFRESH_TOKEN_COOKIE;

/**
 * A service to manage cookies.
 */
@Slf4j
@Service
public class CookieService {

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
        log.debug("Setting access token cookie with value: {}", accessToken.getValue());
        response.addCookie(createSecureCookie(ACCESS_TOKEN_COOKIE, accessToken.getValue(), accessToken.getExpiresAt()));
    }

    /**
     * Sets the refresh token as a cookie in the response.
     *
     * @param response     the response to set the cookie in
     * @param refreshToken the refresh token
     */
    public void setRefreshTokenCookie(final HttpServletResponse response, final Token refreshToken) {
        log.debug("Setting refresh token cookie with value: {}", refreshToken.getValue());
        response.addCookie(createSecureCookie(REFRESH_TOKEN_COOKIE, refreshToken.getValue(), refreshToken.getExpiresAt()));
    }

    /**
     * Clears the access and refresh token cookies in the response.
     *
     * @param response the response to clear the cookies in
     */
    public void clearAuthCookies(final HttpServletResponse response) {
        response.addCookie(createSecureCookie(ACCESS_TOKEN_COOKIE, "", LocalDateTime.now().minusDays(1)));
        response.addCookie(createSecureCookie(REFRESH_TOKEN_COOKIE, "", LocalDateTime.now().minusDays(1)));
    }

    private Cookie createSecureCookie(final String name, final String value, final LocalDateTime expiresAt) {
        final Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) LocalDateTime.now().until(expiresAt, SECONDS));
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}
