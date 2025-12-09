package io.github.eventify.api.user.controller;


import io.github.eventify.api.authentication.model.request.LoginRequest;
import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.request.ForgotPasswordRequest;
import io.github.eventify.api.user.model.request.UpdatePasswordRequest;
import io.github.eventify.common.exception.ApiErrorCode;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.exception.resource.ApiErrorResponseResource;
import io.github.jframe.exception.resource.ValidationErrorResponseResource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.api.user.model.validator.ChangePasswordValidator.*;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Password Management")
public class PasswordControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should request password reset successfully")
    public void shouldRequestPasswordResetSuccessfully() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: Requesting a password reset
        final MockHttpServletRequestBuilder request = post(PUBLIC_REQUEST_PASSWORD_RESET_PATH)
            .param("email", user.getEmail());

        // When: Making the request
        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: The user should have a password reset token
        final Token resetToken = getPasswordResetToken(user);
        assertThat(resetToken, notNullValue());
    }

    @Test
    @DisplayName("Should not reveal existence of non-existent email on password reset request")
    public void shouldReturnNoContentForNonExistentEmail() throws Exception {
        // Given: Requesting a password reset for non-existent email
        final MockHttpServletRequestBuilder request = post(PUBLIC_REQUEST_PASSWORD_RESET_PATH)
            .param("email", "nonexistent@test.com");

        // When: Making the request
        final ResultActions response = mockMvc.perform(request);

        // Then: The response should still be NO_CONTENT for security
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should be able to reset password with valid token")
    public void shouldResetPasswordWithValidToken() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: Request a password reset
        final MockHttpServletRequestBuilder passwordResetRequest = post(PUBLIC_REQUEST_PASSWORD_RESET_PATH)
            .param("email", user.getEmail());

        // When: Requesting a password reset
        mockMvc.perform(passwordResetRequest);

        // And: Get the reset token
        final Token resetToken = getPasswordResetToken(user);
        final ForgotPasswordRequest request = aForgotPasswordRequest()
            .setToken(resetToken.getValue());

        // And: Resetting password with valid token
        final MockHttpServletRequestBuilder resetRequest = post(PUBLIC_RESET_PASSWORD_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        // Then: Reset password is initiated
        final ResultActions response = mockMvc.perform(resetRequest);

        // Then: The response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: The user should be able to log in with new password
        final LoginRequest loginRequest = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(NEW_PASSWORD);

        // When: Logging in with new password
        final MockHttpServletRequestBuilder loginReq = post(LOGIN_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(loginRequest));

        // Then: The login is requested
        final ResultActions loginResponse = mockMvc.perform(loginReq);

        // Then: The response should be OK
        loginResponse.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should fail to reset password with invalid token")
    public void shouldFailResetPasswordWithInvalidToken() throws Exception {
        // Given: A forgot password request with invalid token
        final ForgotPasswordRequest request = aForgotPasswordRequest()
            .setToken("invalid-token");

        // And: Resetting password with invalid token
        final MockHttpServletRequestBuilder resetRequest = post(PUBLIC_RESET_PASSWORD_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        // When: Resetting password with invalid token
        final ResultActions response = mockMvc.perform(resetRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Response should contain the error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), is(ApiErrorCode.TOKEN_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should fail to reset password with mismatched confirmation")
    public void shouldFailResetPasswordWithMismatch() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: Request a password reset
        mockMvc.perform(
            post(PUBLIC_REQUEST_PASSWORD_RESET_PATH)
                .param("email", user.getEmail())
        );

        // And: Get the reset token with mismatched passwords
        final Token resetToken = getPasswordResetToken(user);
        final ForgotPasswordRequest request = aForgotPasswordRequest();
        request.setConfirmPassword("DifferentPassword123!@#");
        request.setToken(resetToken.getValue());

        // And: Resetting password with mismatched confirmation
        final MockHttpServletRequestBuilder resetRequest = post(PUBLIC_RESET_PASSWORD_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        // When: Resetting password with mismatched confirmation
        final ResultActions response = mockMvc.perform(resetRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Response should contain validation errors
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(error.getErrors().getFirst().getField(), is(CONFIRM_PASSWORD));
        assertThat(error.getErrors().getFirst().getCode(), is(PASSWORD_DOES_NOT_MATCH_THE_CONFIRMATION));
    }

    @Test
    @DisplayName("Should update password successfully when authenticated")
    public void shouldUpdatePasswordSuccessfully() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: An update password request
        final UpdatePasswordRequest request = anUpdatePasswordRequest()
            .setOldPassword(TEST_PASSWORD);

        // And: Updating password
        final MockHttpServletRequestBuilder updateRequest = post(UPDATE_PASSWORD_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        // When: Updating password
        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: The response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: The user should be able to login with new password
        final LoginRequest loginRequest = new LoginRequest()
            .setEmail(user.getEmail())
            .setPassword(NEW_PASSWORD);

        // When: Logging in with new password
        final MockHttpServletRequestBuilder loginReq = post(LOGIN_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(loginRequest));

        // Then: The login is requested
        final ResultActions loginResponse = mockMvc.perform(loginReq);

        // Then: The response should be OK
        loginResponse.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should fail to update password with incorrect old password")
    public void shouldFailUpdatePasswordWithIncorrectOldPassword() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: An update password request with incorrect old password
        final UpdatePasswordRequest request = anUpdatePasswordRequest()
            .setOldPassword("IncorrectOldPassword123!@#");

        // And: Updating password with incorrect old password
        final MockHttpServletRequestBuilder updateRequest = post(UPDATE_PASSWORD_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        // When: Updating password with incorrect old password
        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Response should contain the error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), is(ApiErrorCode.PASSWORD_DOES_NOT_MATCH.getReason()));
    }

    @Test
    @DisplayName("Should fail to update password with invalid format")
    public void shouldFailUpdatePasswordWithInvalidFormat() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: The user is logged in
        final Token accessToken = user.getAccessToken();
        assertThat(accessToken.getValue(), notNullValue());

        // And: An update password request with invalid format
        final UpdatePasswordRequest request = anUpdatePasswordRequest();
        request.setNewPassword("invalid");
        request.setConfirmPassword("invalid");

        // And: Updating password with invalid format
        final MockHttpServletRequestBuilder updateRequest = post(UPDATE_PASSWORD_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + accessToken.getValue())
            .content(toJson(request));

        // When: Updating password with invalid format
        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: The response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Response should contain validation errors
        final String content = response.andReturn().getResponse().getContentAsString();
        final ValidationErrorResponseResource error = fromJson(content, ValidationErrorResponseResource.class);

        assertThat(error.getErrors().getFirst().getField(), is("password"));
        assertThat(error.getErrors().getFirst().getCode(), is(PASSWORD_IS_NOT_STRONG_ENOUGH));
    }
}
