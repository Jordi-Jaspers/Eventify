package org.jordijaspers.eventify.api.user.controller;

import java.util.List;

import org.hawaiiframework.web.resource.ApiErrorResponseResource;
import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.authentication.model.response.UserDetailsResponse;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.model.request.UpdateEmailRequest;
import org.jordijaspers.eventify.api.user.model.request.UpdateUserDetailsRequest;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.type.TypeReference;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jordijaspers.eventify.api.Paths.*;
import static org.jordijaspers.eventify.common.constants.Constants.Security.BEARER;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.fromJson;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.toJson;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return all users when requested")
    public void shouldReturnAllUsersWhenRequested() throws Exception {
        // Given: An authenticated user with manager authority
        final User user = aValidatedUserWithAuthority(Authority.MANAGER);

        // And: Multiple users exist
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // When: Requesting all users
        final MockHttpServletRequestBuilder request = get(USERS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        // And: Making the request
        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain all users
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<UserDetailsResponse> usersList = fromJson(content, new TypeReference<>() {});
        final List<UserDetailsResponse> users = usersList.stream()
            .filter(u -> u.getId().equals(user1.getId()) || u.getId().equals(user2.getId()))
            .toList();

        assertThat(users.size(), equalTo(2));
        users.forEach(u -> {
            assertThat(u.getId(), anyOf(is(user1.getId()), is(user2.getId())));
            assertThat(u.getEmail(), anyOf(is(user1.getEmail()), is(user2.getEmail())));
        });
    }

    @Test
    @DisplayName("Should return user details for authenticated user")
    public void shouldReturnUserDetailsForAuthenticatedUser() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // When: Requesting user details
        final MockHttpServletRequestBuilder request = get(USER_DETAILS)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain the user details
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDetailsResponse userDetails = fromJson(content, UserDetailsResponse.class);

        assertThat(userDetails.getId(), is(user.getId()));
        assertThat(userDetails.getEmail(), is(user.getEmail()));
        assertThat(userDetails.getFirstName(), is(user.getFirstName()));
        assertThat(userDetails.getLastName(), is(user.getLastName()));
    }

    @Test
    @DisplayName("Should update user details successfully")
    public void shouldUpdateUserDetailsSuccessfully() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Update details request
        final UpdateUserDetailsRequest request = anUpdateUserDetailsRequest();

        // When: Updating user details
        final MockHttpServletRequestBuilder updateRequest = post(USER_DETAILS)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain updated user details
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDetailsResponse userDetails = fromJson(content, UserDetailsResponse.class);

        assertThat(userDetails.getId(), is(user.getId()));
        assertThat(userDetails.getFirstName(), is(request.getFirstName()));
        assertThat(userDetails.getLastName(), is(request.getLastName()));
    }

    @Test
    @DisplayName("Should update user email successfully")
    public void shouldUpdateUserEmailSuccessfully() throws Exception {
        // Given: An authenticated user
        final User user = aValidatedUser();

        // And: Update email request
        final UpdateEmailRequest request = anUpdateEmailRequest();

        // When: Updating user email
        final MockHttpServletRequestBuilder updateRequest = post(USER_UPDATE_EMAIL_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain updated email
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDetailsResponse userDetails = fromJson(content, UserDetailsResponse.class);

        assertThat(userDetails.getId(), is(user.getId()));
        assertThat(userDetails.getEmail(), is(request.getEmail()));
        assertThat(userDetails.isValidated(), is(false));
    }

    @Test
    @DisplayName("Should not update email when email is already in use")
    public void shouldNotUpdateEmailWhenEmailIsAlreadyInUse() throws Exception {
        // Given: Two validated users
        final User user1 = aValidatedUser();
        final User user2 = aValidatedUser();

        // And: Update email request with existing email
        final UpdateEmailRequest request = new UpdateEmailRequest()
            .setEmail(user2.getEmail());

        // When: Updating user email
        final MockHttpServletRequestBuilder updateRequest = post(USER_UPDATE_EMAIL_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user1.getAccessToken().getValue())
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Should return a BAD_REQUEST error
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Response should contain the error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), is(ApiErrorCode.USER_ALREADY_EXISTS_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should validate email successfully")
    public void shouldValidateEmailSuccessfully() throws Exception {
        // Given: An email validation request
        final UpdateEmailRequest request = anUpdateEmailRequest();

        // When: Validating email
        final MockHttpServletRequestBuilder validationRequest = post(PUBLIC_VALIDATE_EMAIL_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        final ResultActions response = mockMvc.perform(validationRequest);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should indicate email is valid and not in use
        response.andExpect(content().string("false"));
    }

    @Test
    @DisplayName("Should indicate when email is already in use during validation")
    public void shouldIndicateWhenEmailIsAlreadyInUseValidation() throws Exception {
        // Given: An existing validated user
        final User user = aValidatedUser();

        // And: An email validation request with existing email
        final UpdateEmailRequest request = new UpdateEmailRequest()
            .setEmail(user.getEmail());

        // And: Validating email
        final MockHttpServletRequestBuilder validationRequest = post(PUBLIC_VALIDATE_EMAIL_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        // When: Making the request
        final ResultActions response = mockMvc.perform(validationRequest);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should indicate is invalid - email is already in use.
        response.andExpect(content().string("false"));
    }
}
