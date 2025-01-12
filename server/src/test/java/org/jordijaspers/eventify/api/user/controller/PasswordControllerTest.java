package org.jordijaspers.eventify.api.user.controller;

import io.restassured.module.mockmvc.response.MockMvcResponse;

import org.jordijaspers.eventify.api.authentication.model.request.LoginRequest;
import org.jordijaspers.eventify.api.token.model.Token;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.model.request.ForgotPasswordRequest;
import org.jordijaspers.eventify.api.user.model.request.UpdatePasswordRequest;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.*;
import static org.jordijaspers.eventify.api.user.model.validator.ChangePasswordValidator.PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION;
import static org.jordijaspers.eventify.api.user.model.validator.ChangePasswordValidator.PASSWORD_IS_NOT_STRONG_ENOUGH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public final class PasswordControllerTest extends IntegrationTest {

    @Test
    @DisplayName("A user can request a password reset token successfully")
    public void shouldRequestPasswordResetSuccessfully() {
        // Given: A validated user
        final User user = aValidatedUser();

        // When: Requesting a password reset
        final MockMvcResponse response = given()
            .param("email", user.getEmail())
            .when()
            .post(PUBLIC_REQUEST_PASSWORD_RESET_PATH)
            .andReturn();

        // Then: The response should be NO_CONTENT
        response.then().statusCode(SC_NO_CONTENT);

        // And: The user should have a password reset token
        final Token resetToken = getPasswordResetToken(user);
        assertThat(resetToken, notNullValue());
    }

    @Test
    @DisplayName("A non-existent email for password reset returns NO_CONTENT")
    public void shouldReturnNoContentForNonExistentEmail() {
        // When: Requesting a password reset for non-existent email
        final MockMvcResponse response = given()
            .param("email", "nonexistent@test.com")
            .when()
            .post(PUBLIC_REQUEST_PASSWORD_RESET_PATH)
            .andReturn();

        // Then: The response should still be NO_CONTENT for security
        response.then().statusCode(SC_NO_CONTENT);
    }

    @Test
    @DisplayName("A user can reset their password with a valid token")
    public void shouldResetPasswordWithValidToken() {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: Request a password reset
        given()
            .param("email", user.getEmail())
            .when()
            .post(PUBLIC_REQUEST_PASSWORD_RESET_PATH);

        // And: Get the reset token
        final Token resetToken = getPasswordResetToken(user);
        final ForgotPasswordRequest request = aForgotPasswordRequest()
            .setToken(resetToken.getValue());

        // When: Resetting password with valid token
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(PUBLIC_RESET_PASSWORD_PATH)
            .andReturn();

        // Then: The response should be NO_CONTENT
        response.then().statusCode(SC_NO_CONTENT);

        // And: The user should be able to login with new password
        final LoginRequest loginRequest = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(NEW_PASSWORD);

        final MockMvcResponse loginResponse = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(loginRequest)
            .when()
            .post(LOGIN_PATH)
            .andReturn();

        loginResponse.then().statusCode(SC_OK);
    }

    @Test
    @DisplayName("A user cannot reset password with invalid token")
    public void shouldFailResetPasswordWithInvalidToken() {
        // Given: A forgot password request with invalid token
        final ForgotPasswordRequest request = aForgotPasswordRequest()
            .setToken("invalid-token");

        // When: Resetting password with invalid token
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(PUBLIC_RESET_PASSWORD_PATH)
            .andReturn();

        // Then: The response should be BAD_REQUEST
        response.then().statusCode(SC_BAD_REQUEST);
        response.then().body("apiErrorReason", is(ApiErrorCode.TOKEN_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("A password reset fails with password mismatch")
    public void shouldFailResetPasswordWithMismatch() {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: Request a password reset
        given()
            .param("email", user.getEmail())
            .when()
            .post(PUBLIC_REQUEST_PASSWORD_RESET_PATH);

        // And: Get the reset token with mismatched passwords
        final Token resetToken = getPasswordResetToken(user);
        final ForgotPasswordRequest request = aForgotPasswordRequest();
        request.setConfirmPassword("DifferentPassword123!@#");
        request.setToken(resetToken.getValue());

        // When: Resetting password with mismatched confirmation
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(request)
            .when()
            .post(PUBLIC_RESET_PASSWORD_PATH)
            .prettyPeek()
            .andReturn();

        // Then: The response should be BAD_REQUEST
        response.then().statusCode(SC_BAD_REQUEST);
        response.then().body("errors.field", hasItem("confirmPassword"));
        response.then().body(
            "errors.find { error -> error.field == 'confirmPassword' }.code",
            equalTo(PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION)
        );
    }

    @Test
    @DisplayName("An authenticated user can update their password")
    public void shouldUpdatePasswordSuccessfully() {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // Given: An update password request
        final UpdatePasswordRequest request = anUpdatePasswordRequest()
            .setOldPassword(TEST_PASSWORD);

        // When: Updating password
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .body(request)
            .when()
            .post(UPDATE_PASSWORD_PATH)
            .andReturn();

        // Then: The response should be NO_CONTENT
        response.then().statusCode(SC_NO_CONTENT);

        // And: The user should be able to login with new password
        final LoginRequest loginRequest = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(NEW_PASSWORD);

        // When: Logging in with new password
        final MockMvcResponse loginResponse = given()
            .contentType(APPLICATION_JSON_VALUE)
            .body(loginRequest)
            .when()
            .post(LOGIN_PATH)
            .andReturn();

        // Then: The response should be OK
        loginResponse.then().statusCode(SC_OK);
    }

    @Test
    @DisplayName("A user cannot update password with incorrect old password")
    public void shouldFailUpdatePasswordWithIncorrectOldPassword() {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // Given: An update password request with incorrect old password
        final UpdatePasswordRequest request = anUpdatePasswordRequest()
            .setOldPassword("IncorrectOldPassword123!@#");

        // When: Updating password with incorrect old password
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .body(request)
            .when()
            .post(UPDATE_PASSWORD_PATH)
            .andReturn();

        // Then: The response should be BAD_REQUEST
        response.then().statusCode(SC_BAD_REQUEST);
        response.then().body("apiErrorReason", is(ApiErrorCode.PASSWORD_DOES_NOT_MATCH.getReason()));
    }

    @Test
    @DisplayName("A password update fails with invalid password format")
    public void shouldFailUpdatePasswordWithInvalidFormat() {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // Given: An update password request with invalid format
        final UpdatePasswordRequest request = anUpdatePasswordRequest();
        request.setNewPassword("invalid");
        request.setConfirmPassword("invalid");

        // When: Updating password with invalid format
        final MockMvcResponse response = given()
            .contentType(APPLICATION_JSON_VALUE)
            .header(AUTHORIZATION, "Bearer " + accessToken.getValue())
            .body(request)
            .when()
            .post(UPDATE_PASSWORD_PATH)
            .prettyPeek()
            .andReturn();

        // Then: The response should be BAD_REQUEST
        response.then().statusCode(SC_BAD_REQUEST);

        // And: The response should contain the error
        response.then().body("errors.field", hasItem("password"));
        response.then().body(
            "errors.find { error -> error.field == 'password' }.code",
            equalTo(PASSWORD_IS_NOT_STRONG_ENOUGH)
        );
    }
}
