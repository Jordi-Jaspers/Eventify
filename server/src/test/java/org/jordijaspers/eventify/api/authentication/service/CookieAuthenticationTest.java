package org.jordijaspers.eventify.api.authentication.service;

import jakarta.servlet.http.Cookie;

import org.jordijaspers.eventify.api.authentication.model.request.LoginRequest;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.LOGIN_PATH;
import static org.jordijaspers.eventify.api.Paths.OPTIONS_PATH;
import static org.jordijaspers.eventify.common.constants.Constants.Security.ACCESS_TOKEN_COOKIE;
import static org.jordijaspers.eventify.common.constants.Constants.Security.REFRESH_TOKEN_COOKIE;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.toJson;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Cookie Authentication Integration Tests")
public class CookieAuthenticationTest extends IntegrationTest {

    @Test
    @DisplayName("Should set auth cookies when login is successful")
    public void shouldSetAuthCookiesOnSuccessfulLogin() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: A valid login request
        final LoginRequest request = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(TEST_PASSWORD);

        // And: Logging in with valid credentials
        final MockHttpServletRequestBuilder loginRequest = post(LOGIN_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        // When: Making the request
        final ResultActions response = mockMvc.perform(loginRequest);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Should set HTTP-only cookies
        final Cookie accessTokenCookie = response.andReturn().getResponse().getCookie(ACCESS_TOKEN_COOKIE);
        final Cookie refreshTokenCookie = response.andReturn().getResponse().getCookie(REFRESH_TOKEN_COOKIE);

        assertThat(accessTokenCookie, notNullValue());
        assertThat(refreshTokenCookie, notNullValue());
        assertThat(accessTokenCookie.isHttpOnly(), is(true));
        assertThat(refreshTokenCookie.isHttpOnly(), is(true));
        assertThat(accessTokenCookie.getMaxAge(), lessThan(refreshTokenCookie.getMaxAge()));
    }

    @Test
    @DisplayName("Should automatically refresh token when access token is expired")
    public void shouldAutoRefreshExpiredAccessToken() throws Exception {
        // Given: A logged-in user with valid cookies
        final User user = aValidatedUser();

        // And: The user has a valid refresh token
        final String refreshToken = user.getRefreshToken().getValue();
        assertThat(refreshToken, notNullValue());

        // And: We pass the cookies to the request
        final Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE, refreshToken);

        // And: Making a request after access token expiry (simulate by not sending access token)
        final MockHttpServletRequestBuilder request = get(OPTIONS_PATH)
            .cookie(refreshCookie)
            .contentType(APPLICATION_JSON);

        // When: Making the request
        final ResultActions response = mockMvc.perform(request);

        // Then: They should have access
        response.andExpect(status().is(SC_OK));

        // And: Should receive new auth cookies
        Cookie newAccessToken = null;
        Cookie newRefreshToken = null;
        for (final Cookie cookie : response.andReturn().getResponse().getCookies()) {
            if (cookie.getName().equals(ACCESS_TOKEN_COOKIE) && !cookie.getValue().isEmpty()) {
                newAccessToken = cookie;
            }

            if (cookie.getName().equals(REFRESH_TOKEN_COOKIE) && !cookie.getValue().isEmpty()) {
                newRefreshToken = cookie;
            }
        }

        // And: The old token does not exist anymore.
        assertThat(tokenService.findAuthorizationTokenByValue(refreshToken), is(nullValue()));

        // And: New access token should be different from the old one
        assertThat(newAccessToken, notNullValue());
        assertThat(newRefreshToken, is(not(refreshToken)));
    }

    @Test
    @DisplayName("Should fail when both tokens are invalid")
    public void shouldFailWhenBothTokensAreInvalid() throws Exception {
        // When: Making a request with invalid cookies
        final MockHttpServletRequestBuilder request = get(OPTIONS_PATH)
            .cookie(new Cookie(ACCESS_TOKEN_COOKIE, "invalid-token"))
            .cookie(new Cookie(REFRESH_TOKEN_COOKIE, "invalid-refresh-token"))
            .contentType(APPLICATION_JSON);

        // When: Making the request
        final ResultActions response = mockMvc.perform(request);

        // Then: Should receive unauthorized
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should access protected resource with valid cookies")
    public void shouldAccessProtectedResourceWithValidCookies() throws Exception {
        // Given: A logged-in user with valid cookies
        final User user = aValidatedUser();

        // And: The user has valid access and refresh tokens
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        final Token refreshToken = user.getRefreshToken();
        assertThat(refreshToken.getValue(), notNullValue());

        // When: Accessing a protected resource
        final MockHttpServletRequestBuilder request = get(OPTIONS_PATH)
            .cookie(new Cookie(ACCESS_TOKEN_COOKIE, accessToken.getValue()))
            .cookie(new Cookie(REFRESH_TOKEN_COOKIE, refreshToken.getValue()))
            .contentType(APPLICATION_JSON);

        // When: Making the request
        final ResultActions response = mockMvc.perform(request);

        // Then: Should have access
        response.andExpect(status().is(SC_OK));
    }
}
