package io.github.eventify.api.authentication.controller;

import io.github.eventify.api.authentication.model.request.LoginRequest;
import io.github.eventify.api.authentication.model.request.RefreshTokenRequest;
import io.github.eventify.api.authentication.model.request.RegisterUserRequest;
import io.github.eventify.api.authentication.model.response.AuthenticationResponse;
import io.github.eventify.api.authentication.model.response.RegisterResponse;
import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.ApiErrorCode;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.exception.resource.ApiErrorResponseResource;

import java.util.List;
import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.eventify.common.constant.Constants.Security.REFRESH_TOKEN_COOKIE;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Authentication Controller")
public class AuthenticationControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should register a new user successfully")
    public void registerUserSuccess() throws Exception {
        // Given: A valid registration request
        final RegisterUserRequest request = aRegisterRequest();

        // When: Registering a new user
        final MockHttpServletRequestBuilder registerRequest = post(REGISTER_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(registerRequest);

        // Then: The response should be CREATED with valid user data
        response.andExpect(status().is(SC_CREATED));

        // And: The user should be enabled and not validated
        final String content = response.andReturn().getResponse().getContentAsString();
        final RegisterResponse registerResponse = fromJson(content, RegisterResponse.class);

        assertThat(registerResponse.getEmail(), containsString(TEST_EMAIL));
        assertThat(registerResponse.isEnabled(), is(true));
        assertThat(registerResponse.isValidated(), is(false));
    }

    @Test
    @DisplayName("Should not register a user with an existing email")
    public void registerUserWithExistingEmailFails() throws Exception {
        // Given: An existing user
        final User user = anUnvalidatedUser();

        // And: A registration request with the same email
        final RegisterUserRequest request = aRegisterRequest()
            .setEmail(user.getEmail());

        // When: Registering with the same email
        final MockHttpServletRequestBuilder registerRequest = post(REGISTER_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(registerRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: The response should contain an error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), is(ApiErrorCode.USER_ALREADY_EXISTS_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials and is not validated")
    public void loginSuccessWithValidCredentialsNotValidated() throws Exception {
        // Given: A registered user
        final User user = anUnvalidatedUser();

        // And: A valid login request
        final LoginRequest request = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(TEST_PASSWORD);

        // When: Logging in with valid credentials
        final MockHttpServletRequestBuilder loginRequest = post(LOGIN_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(loginRequest);

        // Then: The response should be OK with valid tokens
        response.andExpect(status().is(SC_OK));

        // And: The response should contain access and refresh tokens
        final String content = response.andReturn().getResponse().getContentAsString();
        final AuthenticationResponse authenticationResponse = fromJson(content, AuthenticationResponse.class);

        assertThat(authenticationResponse.getEmail(), is(user.getEmail()));
        assertThat(authenticationResponse.getAccessToken(), notNullValue());
        assertThat(authenticationResponse.getRefreshToken(), notNullValue());

        // And: The user should be enabled and not validated
        assertThat(authenticationResponse.isEnabled(), is(true));
        assertThat(authenticationResponse.isValidated(), is(false));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials and is validated")
    public void loginSuccessWithValidCredentialsIsValidated() throws Exception {
        // Given: A registered and validated user
        final User user = aValidatedUser();

        // And: A valid login request
        final LoginRequest request = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(TEST_PASSWORD);

        // When: Logging in with valid credentials
        final MockHttpServletRequestBuilder loginRequest = post(LOGIN_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(loginRequest);

        // Then: The response should be OK with valid tokens
        response.andExpect(status().is(SC_OK));

        // And: The response should contain access and refresh tokens
        final String content = response.andReturn().getResponse().getContentAsString();
        final AuthenticationResponse authenticationResponse = fromJson(content, AuthenticationResponse.class);

        assertThat(authenticationResponse.getEmail(), is(user.getEmail()));
        assertThat(authenticationResponse.getAccessToken(), notNullValue());
        assertThat(authenticationResponse.getRefreshToken(), notNullValue());

        // And: The user should be enabled and validated
        assertThat(authenticationResponse.isEnabled(), is(true));
        assertThat(authenticationResponse.isValidated(), is(true));
    }

    @Test
    @DisplayName("Should not be able to login with invalid credentials")
    public void loginWithInvalidCredentialsFails() throws Exception {
        // Given: A registered user
        final User user = aValidatedUser();

        // And: An invalid login request
        final LoginRequest request = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword("invalid-password");

        // When: Logging in with invalid credentials
        final MockHttpServletRequestBuilder loginRequest = post(LOGIN_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(loginRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: The response should contain an error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), is(ApiErrorCode.INVALID_CREDENTIALS.getReason()));
    }

    @Test
    @DisplayName("Should not be able to login with a locked user")
    public void loginWithLockedUserFails() throws Exception {
        // Given: A locked user
        final User user = aLockedUser();

        // And: A valid login request
        final LoginRequest request = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(TEST_PASSWORD);

        // When: Logging in with valid credentials
        final MockHttpServletRequestBuilder loginRequest = post(LOGIN_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(loginRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: The response should contain an error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), containsString(ApiErrorCode.USER_LOCKED_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should logout and invalidate only the current session, leaving other sessions intact")
    public void logoutInvalidatesOnlyCurrentSession() throws Exception {
        // Given: A registered and validated user
        final User user = aValidatedUser();

        // And: A valid login request
        final LoginRequest loginRequest = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(TEST_PASSWORD);

        // When: Logging in from session A
        final ResultActions sessionALoginResponse = mockMvc.perform(
            post(LOGIN_PATH)
                .contentType(APPLICATION_JSON)
                .content(toJson(loginRequest))
        );
        sessionALoginResponse.andExpect(status().is(SC_OK));

        final AuthenticationResponse sessionAAuth = fromJson(
            sessionALoginResponse.andReturn().getResponse().getContentAsString(),
            AuthenticationResponse.class
        );
        final String sessionARefreshToken = sessionAAuth.getRefreshToken();
        final String sessionAAccessToken = sessionAAuth.getAccessToken();

        // And: Logging in again from session B (produces a second refresh token)
        final ResultActions sessionBLoginResponse = mockMvc.perform(
            post(LOGIN_PATH)
                .contentType(APPLICATION_JSON)
                .content(toJson(loginRequest))
        );
        sessionBLoginResponse.andExpect(status().is(SC_OK));

        final AuthenticationResponse sessionBAuth = fromJson(
            sessionBLoginResponse.andReturn().getResponse().getContentAsString(),
            AuthenticationResponse.class
        );
        final String sessionBRefreshToken = sessionBAuth.getRefreshToken();

        // And: Both sessions exist in the database (plus the verifyEmail session = 3 total)
        assertThat(getRefreshTokens(user), hasSize(3));

        // When: Logging out from session A using session A's access token and refresh-token cookie
        final MockHttpServletRequestBuilder logoutRequest = get(LOGOUT_PATH)
            .header(AUTHORIZATION, BEARER + sessionAAccessToken)
            .cookie(
                new jakarta.servlet.http.Cookie(
                    io.github.eventify.common.constant.Constants.Security.REFRESH_TOKEN_COOKIE,
                    sessionARefreshToken
                )
            );

        final ResultActions logoutResponse = mockMvc.perform(logoutRequest);

        // Then: The response should be NO_CONTENT
        logoutResponse.andExpect(status().is(SC_NO_CONTENT));

        // And: Session B's refresh token should still exist and be valid (plus the verifyEmail session = 2 remaining)
        final List<Token> remainingTokens = getRefreshTokens(user);
        assertThat(remainingTokens, hasSize(2));
        assertThat(
            remainingTokens.stream().anyMatch(
                t -> t.getValueHash().equals(
                    io.github.eventify.common.util.HashUtil.sha256(sessionBRefreshToken)
                )
            ),
            is(true)
        );
    }

    @Test
    @DisplayName("Should also be able to logout when not logged in")
    public void logoutWhenNotLoggedInSucceeds() throws Exception {
        // When: Logging out without being logged in
        final MockHttpServletRequestBuilder logoutRequest = get(LOGOUT_PATH);

        final ResultActions logoutResponse = mockMvc.perform(logoutRequest);

        // Then: The response should be NO_CONTENT
        logoutResponse.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should be able to refresh access token with a valid refresh token")
    public void refreshTokenSuccess() throws Exception {
        // Given: A registered and validated user
        final User user = aValidatedUser();

        // And: A valid login request
        final LoginRequest request = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(TEST_PASSWORD);

        // When: Logging in with valid credentials
        final MockHttpServletRequestBuilder loginRequest = post(LOGIN_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions loginResponse = mockMvc.perform(loginRequest);

        // Then: The response should be OK with valid tokens
        loginResponse.andExpect(status().is(SC_OK));

        // And: The response should contain access and refresh tokens
        final String loginContent = loginResponse.andReturn().getResponse().getContentAsString();
        final AuthenticationResponse authenticationResponse = fromJson(loginContent, AuthenticationResponse.class);
        assertThat(authenticationResponse.getRefreshToken(), notNullValue());

        // When: Creating a request to refresh the access token
        final RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest()
            .setRefreshToken(authenticationResponse.getRefreshToken());

        // And: Refreshing the access token
        final MockHttpServletRequestBuilder refreshRequest = post(TOKEN_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(refreshTokenRequest));

        final ResultActions refreshResponse = mockMvc.perform(refreshRequest);

        // Then: The response should be OK with valid tokens
        refreshResponse.andExpect(status().is(SC_OK));

        // And: The response should contain access and refresh tokens
        final String refreshContent = refreshResponse.andReturn().getResponse().getContentAsString();
        final AuthenticationResponse refreshedAuthenticationResponse = fromJson(refreshContent, AuthenticationResponse.class);
        assertThat(refreshedAuthenticationResponse.getAccessToken(), notNullValue());
        assertThat(refreshedAuthenticationResponse.getRefreshToken(), notNullValue());
    }

    @Test
    @DisplayName("Should not be able to refresh access token with an invalid refresh token")
    public void refreshTokenWithInvalidTokenFails() throws Exception {
        // Given: An invalid refresh token
        final RefreshTokenRequest request = new RefreshTokenRequest()
            .setRefreshToken("invalid-token");

        // When: Refreshing the access token with invalid refresh token
        final MockHttpServletRequestBuilder refreshRequest = post(TOKEN_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(refreshRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: The response should contain an error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), is(ApiErrorCode.INVALID_TOKEN_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should set refresh-token cookie with extended maxAge (~30 days) when rememberMe=true")
    public void loginWithRememberMeSetsExtendedRefreshCookieMaxAge() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: A login request with rememberMe=true
        final LoginRequest request = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(TEST_PASSWORD)
            .setRememberMe(true);

        // When: Logging in with rememberMe=true
        final MockHttpServletRequestBuilder loginRequest = post(LOGIN_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(loginRequest);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: The refresh-token cookie maxAge should be approximately 30 days (±60s tolerance)
        final Cookie refreshTokenCookie = response.andReturn().getResponse().getCookie(REFRESH_TOKEN_COOKIE);
        assertThat(refreshTokenCookie, is(notNullValue()));

        final int minExpected = 29 * 86400;
        final int maxExpected = 30 * 86400 + 60;
        assertThat(refreshTokenCookie.getMaxAge(), is(greaterThanOrEqualTo(minExpected)));
        assertThat(refreshTokenCookie.getMaxAge(), is(lessThanOrEqualTo(maxExpected)));
    }
}
