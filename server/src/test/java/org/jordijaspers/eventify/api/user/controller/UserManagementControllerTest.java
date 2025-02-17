package org.jordijaspers.eventify.api.user.controller;

import org.hawaiiframework.web.resource.ApiErrorResponseResource;
import org.jordijaspers.eventify.api.authentication.model.Authority;
import org.jordijaspers.eventify.api.authentication.model.response.UserDetailsResponse;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.model.request.UpdateAuthorityRequest;
import org.jordijaspers.eventify.common.exception.ApiErrorCode;
import org.jordijaspers.eventify.support.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jordijaspers.eventify.api.Paths.*;
import static org.jordijaspers.eventify.common.constants.Constants.Security.BEARER;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.fromJson;
import static org.jordijaspers.eventify.support.util.ObjectMapperUtil.toJson;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserManagementControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should lock user successfully")
    public void shouldLockUserSuccessfully() throws Exception {
        // Given: A Logged in admin
        final User admin = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: A validated user
        final User user = aValidatedUser();

        // And: Locking the user request
        final MockHttpServletRequestBuilder request = post(LOCK_USER_PATH, user.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        // When: Locking the user
        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should indicate user is locked
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDetailsResponse userDetails = fromJson(content, UserDetailsResponse.class);
        assertThat(userDetails.isEnabled(), is(false));

        // And: User should not be able to authenticate
        assertThat(userRepository.findById(user.getId()).orElseThrow().isEnabled(), is(false));
    }

    @Test
    @DisplayName("Should unlock user successfully")
    public void shouldUnlockUserSuccessfully() throws Exception {
        // Given: A Logged in admin
        final User admin = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: A locked user
        final User user = aLockedUser();

        // When: Unlocking the user
        final MockHttpServletRequestBuilder request = post(UNLOCK_USER_PATH, user.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        // And: Making the request
        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should indicate user is unlocked
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDetailsResponse userDetails = fromJson(content, UserDetailsResponse.class);
        assertThat(userDetails.isEnabled(), is(true));

        // And: User should be able to authenticate
        assertThat(userRepository.findById(user.getId()).orElseThrow().isEnabled(), is(true));
    }

    @Test
    @DisplayName("Should return error when locking non-existing user")
    public void shouldReturnErrorWhenLockingNonExistingUser() throws Exception {
        // Given: A Logged in admin
        final User admin = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: Trying to lock the non-existing user
        final MockHttpServletRequestBuilder request = post(LOCK_USER_PATH, 99999L)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        // When: Making the request
        final ResultActions response = mockMvc.perform(request);

        // Then: Should return a BAD_REQUEST error
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Response should contain the error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), is(ApiErrorCode.USER_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should update user authority successfully")
    public void shouldUpdateUserAuthoritySuccessfully() throws Exception {
        // Given: A Logged in admin
        final User admin = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: A validated user
        final User user = aValidatedUser();

        // And: An authority update request
        final UpdateAuthorityRequest request = anUpdateAuthorityRequest(Authority.ADMIN);

        // And: Updating the user's authority
        final MockHttpServletRequestBuilder updateRequest = post(USER_PATH, user.getId())
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        // When: Making the request
        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Response should be successful
        response.andExpect(status().is(SC_OK));

        // And: Response should contain updated authority
        final String content = response.andReturn().getResponse().getContentAsString();
        final UserDetailsResponse userDetails = fromJson(content, UserDetailsResponse.class);
        assertThat(userDetails.getAuthority(), is(Authority.ADMIN.getName().toUpperCase()));
    }

    @Test
    @DisplayName("Should return error when updating authority for non-existing user")
    public void shouldReturnErrorWhenUpdatingAuthorityForNonExistingUser() throws Exception {
        // Given: A Logged in admin
        final User admin = aValidatedUserWithAuthority(Authority.ADMIN);

        // And: An authority update request
        final UpdateAuthorityRequest request = anUpdateAuthorityRequest(Authority.ADMIN);

        // And: Trying to update authority of non-existing user
        final MockHttpServletRequestBuilder updateRequest = post(USER_PATH, 99999L)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        // When: Making the request
        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Should return a BAD_REQUEST error
        response.andExpect(status().is(SC_BAD_REQUEST));

        // And: Response should contain the error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getApiErrorReason(), is(ApiErrorCode.USER_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should return error when updating authority without proper authorization")
    public void shouldReturnErrorWhenUpdatingAuthorityWithoutProperAuthorization() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: An authority update request
        final UpdateAuthorityRequest request = anUpdateAuthorityRequest(Authority.ADMIN);

        // And: Trying to update authority without proper authorization
        final MockHttpServletRequestBuilder updateRequest = post(USER_PATH, user.getId())
            .contentType(APPLICATION_JSON)
            .content(toJson(request));

        // When: Making the request
        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Should return an UNAUTHORIZED error
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }
}
