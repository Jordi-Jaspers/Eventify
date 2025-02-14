package org.jordijaspers.eventify.api.authentication.controller;

import org.hawaiiframework.repository.DataNotFoundException;
import org.hawaiiframework.web.resource.ApiErrorResponseResource;
import org.jordijaspers.eventify.api.authentication.model.request.LoginRequest;
import org.jordijaspers.eventify.api.authentication.model.request.RefreshTokenRequest;
import org.jordijaspers.eventify.api.authentication.model.request.RegisterUserRequest;
import org.jordijaspers.eventify.api.authentication.model.response.RegisterResponse;
import org.jordijaspers.eventify.api.authentication.model.response.UserResponse;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.*;
import static org.jordijaspers.eventify.common.constants.Constants.Security.BEARER;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.fromJson;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.toJson;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuthenticationController Integration Tests")
public class AuthenticationControllerTest extends IntegrationTest {

    @Test
    @DisplayName("A user can register successfully")
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
    @DisplayName("A user cannot register with an existing email")
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
    @DisplayName("A user can validate their email successfully")
    public void verifyEmailSuccess() throws Exception {
        // Given: A registered user and unvalidated user
        final User user = anUnvalidatedUser();

        // And: the user has a verification token
        final Token verificationToken = getValidationToken(user);

        // When: Verifying email with valid token
        final MockHttpServletRequestBuilder verifyRequest = post(VERIFICATION_PATH)
            .param("token", verificationToken.getValue());

        final ResultActions response = mockMvc.perform(verifyRequest);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: The user should be enabled and validated
        final User updatedUser = getUserDetails(user.getEmail());
        assertThat(updatedUser.isEnabled(), is(true));
        assertThat(updatedUser.isValidated(), is(true));
    }

    @Test
    @DisplayName("A user cannot validate their email with an invalid token")
    public void verifyEmailWithInvalidTokenFails() throws Exception {
        // When: Verifying email with invalid token
        final MockHttpServletRequestBuilder verifyRequest = post(VERIFICATION_PATH)
            .param("token", "invalid-token");

        final ResultActions response = mockMvc.perform(verifyRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: The response should contain an error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), is(ApiErrorCode.TOKEN_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("A user can login successfully with valid credentials, but is not validated")
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
        final UserResponse userResponse = fromJson(content, UserResponse.class);

        assertThat(userResponse.getEmail(), is(user.getEmail()));
        assertThat(userResponse.getAccessToken(), notNullValue());
        assertThat(userResponse.getRefreshToken(), notNullValue());

        // And: The user should be enabled and not validated
        assertThat(userResponse.isEnabled(), is(true));
        assertThat(userResponse.isValidated(), is(false));
    }

    @Test
    @DisplayName("A user can login successfully with valid credentials and is validated")
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
        final UserResponse userResponse = fromJson(content, UserResponse.class);

        assertThat(userResponse.getEmail(), is(user.getEmail()));
        assertThat(userResponse.getAccessToken(), notNullValue());
        assertThat(userResponse.getRefreshToken(), notNullValue());

        // And: The user should be enabled and validated
        assertThat(userResponse.isEnabled(), is(true));
        assertThat(userResponse.isValidated(), is(true));
    }

    @Test
    @DisplayName("A user cannot login with invalid credentials")
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
    @DisplayName("A user cannot login with a locked account")
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
    @DisplayName("A user can logout successfully")
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
        final UserResponse userResponse = fromJson(loginContent, UserResponse.class);
        final String accessToken = userResponse.getAccessToken();

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
    @DisplayName("A user can refresh their access token successfully")
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
        final UserResponse userResponse = fromJson(loginContent, UserResponse.class);
        assertThat(userResponse.getRefreshToken(), notNullValue());

        // When: Creating a request to refresh the access token
        final RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest()
            .setRefreshToken(userResponse.getRefreshToken());

        // And: Refreshing the access token
        final MockHttpServletRequestBuilder refreshRequest = post(TOKEN_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(refreshTokenRequest));

        final ResultActions refreshResponse = mockMvc.perform(refreshRequest);

        // Then: The response should be OK with valid tokens
        refreshResponse.andExpect(status().is(SC_OK));

        // And: The response should contain access and refresh tokens
        final String refreshContent = refreshResponse.andReturn().getResponse().getContentAsString();
        final UserResponse refreshedUserResponse = fromJson(refreshContent, UserResponse.class);
        assertThat(refreshedUserResponse.getAccessToken(), notNullValue());
        assertThat(refreshedUserResponse.getRefreshToken(), notNullValue());
    }

    @Test
    @DisplayName("A user cannot refresh their access token with an invalid refresh token")
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
    @DisplayName("An unvalidated user can request a new validation token")
    public void requestNewValidationTokenSuccess() throws Exception {
        // Given: An unvalidated user
        final User user = anUnvalidatedUser();

        // And: The user has a validation token
        final Token validationToken = getValidationToken(user);
        assertThat(validationToken, notNullValue());

        // When: Requesting a new validation token
        final MockHttpServletRequestBuilder request = post(RESEND_EMAIL_VERIFICATION_PATH)
            .param("email", user.getEmail());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: The user should have a new validation token
        final Token newValidationToken = getValidationToken(user);
        assertThat(newValidationToken, notNullValue());
        assertThat(newValidationToken, not(equalTo(validationToken)));
    }
}
