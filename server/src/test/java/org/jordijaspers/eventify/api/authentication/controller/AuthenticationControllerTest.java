package org.jordijaspers.eventify.api.authentication.controller;

import io.restassured.module.mockmvc.response.MockMvcResponse;

import org.hawaiiframework.repository.DataNotFoundException;
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

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@DisplayName("AuthenticationController Integration Tests")
public class AuthenticationControllerTest extends IntegrationTest {

    @Test
    @DisplayName("A user can register successfully")
    public void registerUserSuccess() {
        // Given: A valid registration request
        final RegisterUserRequest request = aRegisterRequest();

        // When: Registering a new user
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(REGISTER_PATH)
            .andReturn();

        // Then: The response should be CREATED with valid user data
        response.then().statusCode(SC_CREATED);

        // And: The user should be enabled and not validated
        final RegisterResponse registerResponse = response.as(RegisterResponse.class);
        assertThat(registerResponse.getEmail(), containsString(TEST_EMAIL));
        assertThat(registerResponse.isEnabled(), is(true));
        assertThat(registerResponse.isValidated(), is(false));
    }

    @Test
    @DisplayName("A user cannot register with an existing email")
    public void registerUserWithExistingEmailFails() {
        // Given: An existing user
        final User user = anUnvalidatedUser();

        // And: A registration request with the same email
        final RegisterUserRequest request = aRegisterRequest()
            .setEmail(user.getEmail());

        // When: Registering with the same email
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(REGISTER_PATH)
            .andReturn();

        // Then: The response should be BAD_REQUEST
        response.then().statusCode(SC_BAD_REQUEST);

        // And: The response should contain an error message
        response.then().body(containsString(ApiErrorCode.USER_ALREADY_EXISTS_ERROR.getReason()));
    }

    @Test
    @DisplayName("A user can validate their email successfully")
    public void verifyEmailSuccess() {
        // Given: A registered user and unvalidated user
        final User user = anUnvalidatedUser();

        // And: the user has a verification token
        final Token verificationToken = getValidationToken(user);

        // When: Verifying email with valid token
        final MockMvcResponse response = given()
            .param("token", verificationToken.getValue())
            .when()
            .post(VERIFICATION_PATH)
            .andReturn();

        // Then: The response should be OK
        response.then().statusCode(SC_OK);

        // And: The user should be enabled and validated
        final User updatedUser = getUserDetails(user.getEmail());
        assertThat(updatedUser.isEnabled(), is(true));
        assertThat(updatedUser.isValidated(), is(true));
    }

    @Test
    @DisplayName("A user cannot validate their email with an invalid token")
    public void verifyEmailWithInvalidTokenFails() {
        // When: Verifying email with invalid token
        final MockMvcResponse response = given()
            .param("token", "invalid-token")
            .when()
            .post(VERIFICATION_PATH)
            .andReturn();

        // Then: The response should be BAD_REQUEST
        response.then().statusCode(SC_BAD_REQUEST);

        // And: The response should contain an error message
        response.then().body(containsString(ApiErrorCode.TOKEN_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("A user can login successfully with valid credentials, but is not validated")
    public void loginSuccessWithValidCredentialsNotValidated() {
        // Given: A registered user
        final User user = anUnvalidatedUser();

        // And: A valid login request
        final LoginRequest request = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(TEST_PASSWORD);

        // When: Logging in with valid credentials
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(LOGIN_PATH)
            .andReturn();

        // Then: The response should be OK with valid tokens
        response.then().statusCode(SC_OK);

        // And: The response should contain access and refresh tokens
        final UserResponse userResponse = response.as(UserResponse.class);
        assertThat(userResponse.getEmail(), is(user.getEmail()));
        assertThat(userResponse.getAccessToken(), notNullValue());
        assertThat(userResponse.getRefreshToken(), notNullValue());

        // And: The user should be enabled and not validated
        assertThat(userResponse.isEnabled(), is(true));
        assertThat(userResponse.isValidated(), is(false));
    }

    @Test
    @DisplayName("A user can login successfully with valid credentials and is validated")
    public void loginSuccessWithValidCredentialsIsValidated() {
        // Given: A registered and validated user
        final User user = aValidatedUser();

        // And: A valid login request
        final LoginRequest request = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(TEST_PASSWORD);

        // When: Logging in with valid credentials
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(LOGIN_PATH)
            .andReturn();

        // Then: The response should be OK with valid tokens
        response.then().statusCode(SC_OK);

        // And: The response should contain access and refresh tokens
        final UserResponse userResponse = response.as(UserResponse.class);
        assertThat(userResponse.getEmail(), is(user.getEmail()));
        assertThat(userResponse.getAccessToken(), notNullValue());
        assertThat(userResponse.getRefreshToken(), notNullValue());

        // And: The user should be enabled and validated
        assertThat(userResponse.isEnabled(), is(true));
        assertThat(userResponse.isValidated(), is(true));
    }

    @Test
    @DisplayName("A user cannot login with invalid credentials")
    public void loginWithInvalidCredentialsFails() {
        // Given: A registered user
        final User user = aValidatedUser();

        // And: An invalid login request
        final LoginRequest request = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword("invalid-password");

        // When: Logging in with invalid credentials
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(LOGIN_PATH)
            .andReturn();

        // Then: The response should be BAD_REQUEST
        response.then().statusCode(SC_BAD_REQUEST);

        // And: The response should contain an error message
        response.then().body("apiErrorReason", is(ApiErrorCode.INVALID_CREDENTIALS.getReason()));
    }

    @Test
    @DisplayName("A user cannot login with a locked account")
    public void loginWithLockedUserFails() {
        // Given: A locked user
        final User user = aLockedUser();

        // And: A valid login request
        final LoginRequest request = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(TEST_PASSWORD);

        // When: Logging in with valid credentials
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(LOGIN_PATH)
            .andReturn();

        // Then: The response should be BAD_REQUEST
        response.then().statusCode(SC_BAD_REQUEST);

        // And: The response should contain an error message
        response.then().body("apiErrorReason", containsString(ApiErrorCode.USER_LOCKED_ERROR.getReason()));
    }

    @Test
    @DisplayName("A user can logout successfully")
    public void logoutSuccess() {
        // Given: A registered and validated user
        final User user = aValidatedUser();

        // And: A valid login request
        final LoginRequest request = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(TEST_PASSWORD);

        // When: Logging in with valid credentials
        final MockMvcResponse loginResponse = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(LOGIN_PATH)
            .andReturn();

        // Then: The response should be OK with valid tokens
        loginResponse.then().statusCode(SC_OK);

        // And: The database should contain the refresh token for the user
        assertThat(getRefreshToken(user), notNullValue());

        // When: The token is extracted from the response
        final UserResponse userResponse = loginResponse.as(UserResponse.class);
        final String accessToken = userResponse.getAccessToken();

        // And: Logging out
        final MockMvcResponse logoutResponse = given()
            .header(AUTHORIZATION, "Bearer " + accessToken)
            .when()
            .get(LOGOUT_PATH)
            .andReturn();

        // Then: The response should be NO_CONTENT
        logoutResponse.then().statusCode(SC_NO_CONTENT);

        // And: The database should throw an exception when trying to retrieve the refresh token
        assertThrows(DataNotFoundException.class, () -> getRefreshToken(user));
    }

    @Test
    @DisplayName("A user can refresh their access token successfully")
    public void refreshTokenSuccess() {
        // Given: A registered and validated user
        final User user = aValidatedUser();

        // And: A valid login request
        final LoginRequest request = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(TEST_PASSWORD);

        // When: Logging in with valid credentials
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(LOGIN_PATH)
            .andReturn();

        // Then: The response should be OK with valid tokens
        response.then().statusCode(SC_OK);

        // And: The response should contain access and refresh tokens
        final UserResponse userResponse = response.as(UserResponse.class);
        assertThat(userResponse.getRefreshToken(), notNullValue());

        // When: Creating a request to refresh the access token
        final RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest()
            .setRefreshToken(userResponse.getRefreshToken());

        // And: Refreshing the access token
        final MockMvcResponse refreshResponse = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(refreshTokenRequest)
            .when()
            .post(TOKEN_PATH)
            .andReturn();

        // Then: The response should be OK with valid tokens
        refreshResponse.then().statusCode(SC_OK);

        // And: The response should contain access and refresh tokens
        final UserResponse refreshedUserResponse = refreshResponse.as(UserResponse.class);
        assertThat(refreshedUserResponse.getAccessToken(), notNullValue());
        assertThat(refreshedUserResponse.getRefreshToken(), notNullValue());
    }

    @Test
    @DisplayName("A user cannot refresh their access token with an invalid refresh token")
    public void refreshTokenWithInvalidTokenFails() {
        // Given: An invalid refresh token
        final RefreshTokenRequest request = new RefreshTokenRequest()
            .setRefreshToken("invalid-token");

        // When: Refreshing the access token with invalid refresh token
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(TOKEN_PATH)
            .andReturn();

        // Then: The response should be BAD_REQUEST
        response.then().statusCode(SC_BAD_REQUEST);

        // And: The response should contain an error message
        response.then().body("apiErrorReason", is(ApiErrorCode.INVALID_TOKEN_ERROR.getReason()));
    }

    @Test
    @DisplayName("An unvalidated user can request a new validation token")
    public void requestNewValidationTokenSuccess() {
        // Given: An unvalidated user
        final User user = anUnvalidatedUser();

        // And: The user has a validation token
        final Token validationToken = getValidationToken(user);
        assertThat(validationToken, notNullValue());

        // When: Requesting a new validation token
        final MockMvcResponse response = given()
            .param("email", user.getEmail())
            .when()
            .post(RESEND_EMAIL_VERIFICATION_PATH)
            .andReturn();

        // Then: The response should be NO_CONTENT
        response.then().statusCode(SC_NO_CONTENT);

        // And: The user should have a new validation token
        final Token newValidationToken = getValidationToken(user);
        assertThat(newValidationToken, notNullValue());
        assertThat(newValidationToken, not(equalTo(validationToken)));
    }
}
