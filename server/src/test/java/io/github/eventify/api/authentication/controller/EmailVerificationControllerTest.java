package io.github.eventify.api.authentication.controller;

import io.github.eventify.api.token.model.Token;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.ApiErrorCode;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.exception.core.DataNotFoundException;
import io.github.jframe.exception.resource.ApiErrorResponseResource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.RESEND_EMAIL_VERIFICATION_PATH;
import static io.github.eventify.api.Paths.VERIFICATION_PATH;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Email Verification Controller")
public class EmailVerificationControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should verify email successfully with a valid token")
    public void verifyEmailSuccess() throws Exception {
        // Given: A registered user and unvalidated user
        final User user = anUnvalidatedUser();

        // And: the user has a verification token
        final Token verificationToken = getValidationToken(user);

        // When: Verifying email with valid token
        final MockHttpServletRequestBuilder verifyRequest = post(VERIFICATION_PATH)
            .param("token", verificationToken.getRawValue());

        final ResultActions response = mockMvc.perform(verifyRequest);

        // Then: The response should be OK
        response.andExpect(status().is(SC_OK));

        // And: The user should be enabled and validated
        final User updatedUser = getUserDetails(user.getEmail());
        assertThat(updatedUser.isEnabled(), is(true));
        assertThat(updatedUser.isValidated(), is(true));
    }

    @Test
    @DisplayName("Should not verify email with an invalid token")
    public void verifyEmailWithInvalidTokenFails() throws Exception {
        // When: Verifying email with invalid token
        final MockHttpServletRequestBuilder verifyRequest = post(VERIFICATION_PATH)
            .param("token", "invalid-token");

        final ResultActions response = mockMvc.perform(verifyRequest);

        // Then: The response should be NOT_FOUND
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: The response should contain an error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), is(ApiErrorCode.TOKEN_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should request a new email validation token successfully")
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

    @Test
    @DisplayName("Should not reveal if email is invalid when requesting new validation token")
    public void requestNewValidationTokenWithInvalidEmailSucceeds() throws Exception {
        // When: Requesting a new validation token with an invalid email
        final MockHttpServletRequestBuilder request = post(RESEND_EMAIL_VERIFICATION_PATH)
            .param("email", "invalid-email");

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));
    }

    @Test
    @DisplayName("Should not create a new validation token for an already validated user")
    public void requestNewValidationTokenForValidatedUserFails() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();

        // When: Requesting a new validation token
        final MockHttpServletRequestBuilder request = post(RESEND_EMAIL_VERIFICATION_PATH)
            .param("email", user.getEmail());

        final ResultActions response = mockMvc.perform(request);

        // Then: The response should be NO_CONTENT
        response.andExpect(status().is(SC_NO_CONTENT));

        // And: The user should not have a validation token
        assertThrows(DataNotFoundException.class, () -> getValidationToken(user));
    }
}
