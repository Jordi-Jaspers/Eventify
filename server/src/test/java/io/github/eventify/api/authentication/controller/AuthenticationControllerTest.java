package io.github.eventify.api.authentication.controller;

import io.github.eventify.api.authentication.model.request.LoginRequest;
import io.github.eventify.api.authentication.model.request.RefreshTokenRequest;
import io.github.eventify.api.authentication.model.request.RegisterUserRequest;
import io.github.eventify.api.authentication.model.response.AuthenticationResponse;
import io.github.eventify.api.authentication.model.response.RegisterResponse;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.ApiErrorCode;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.exception.core.DataNotFoundException;
import io.github.jframe.exception.resource.ApiErrorResponseResource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    @DisplayName("Should logout successfully and invalidate the refresh token")
    public void logoutSuccess() throws Exception {
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

        // And: The database should contain the refresh token for the user
        assertThat(getRefreshToken(user), notNullValue());

        // When: The token is extracted from the response
        final String loginContent = loginResponse.andReturn().getResponse().getContentAsString();
        final AuthenticationResponse authenticationResponse = fromJson(loginContent, AuthenticationResponse.class);
        final String accessToken = authenticationResponse.getAccessToken();

        // And: Logging out
        final MockHttpServletRequestBuilder logoutRequest = get(LOGOUT_PATH)
            .header(AUTHORIZATION, BEARER + accessToken);

        // And: The request is made
        final ResultActions logoutResponse = mockMvc.perform(logoutRequest);

        // Then: The response should be NO_CONTENT
        logoutResponse.andExpect(status().is(SC_NO_CONTENT));

        // And: The database should throw an exception when trying to retrieve the refresh token
        assertThrows(DataNotFoundException.class, () -> getRefreshToken(user));
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
}
