package io.github.eventify.api.admin.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.request.UpdateRoleRequest;
import io.github.eventify.api.user.model.response.UserDetailsResponse;
import io.github.eventify.common.exception.ApiErrorCode;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.exception.resource.ApiErrorResponseResource;
import tools.jackson.core.type.TypeReference;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.api.authentication.model.Role.ADMIN;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.anyOf;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Admin User Controller - Management")
public class AdminUserManagementControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return all users when requested")
    public void shouldReturnAllUsersWhenRequested() throws Exception {
        // Given: An authenticated user with manager authority
        final User user = aValidatedUserWithRole(Role.ADMIN);

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
    @DisplayName("Should return error when requesting users without proper authorization")
    public void shouldReturnErrorWhenRequestingUsersWithoutProperAuthorization() throws Exception {
        // Given: A validated user without manager authority
        final User user = aValidatedUser();

        // When: Requesting all users without proper authorization
        final MockHttpServletRequestBuilder request = get(USERS_PATH)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + user.getAccessToken().getValue());

        // And: Making the request
        final ResultActions response = mockMvc.perform(request);

        // Then: Should return an FORBIDDEN error
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should lock user successfully")
    public void shouldLockUserSuccessfully() throws Exception {
        // Given: A Logged in admin
        final User admin = aValidatedUserWithRole(ADMIN);

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
        final User admin = aValidatedUserWithRole(ADMIN);

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
        final User admin = aValidatedUserWithRole(ADMIN);

        // And: Trying to lock the non-existing user
        final MockHttpServletRequestBuilder request = post(LOCK_USER_PATH, 99999L)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        // When: Making the request
        final ResultActions response = mockMvc.perform(request);

        // Then: Should return a BAD_REQUEST error
        response.andExpect(status().is(SC_NOT_FOUND));

        // And: Response should contain the error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(ApiErrorCode.USER_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should update user role successfully")
    public void shouldUpdateUserAuthoritySuccessfully() throws Exception {
        // Given: A Logged in admin
        final User admin = aValidatedUserWithRole(ADMIN);

        // And: A validated user
        final User user = aValidatedUser();

        // And: An authority update request
        final UpdateRoleRequest request = anUpdateRoleRequest(ADMIN);

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
        assertThat(userDetails.getRole(), is(ADMIN));
    }

    @Test
    @DisplayName("Should return error when updating authority for non-existing user")
    public void shouldReturnErrorWhenUpdatingAuthorityForNonExistingUser() throws Exception {
        // Given: A Logged in admin
        final User admin = aValidatedUserWithRole(ADMIN);

        // And: An authority update request
        final UpdateRoleRequest request = anUpdateRoleRequest(ADMIN);

        // And: Trying to update authority of non-existing user
        final MockHttpServletRequestBuilder updateRequest = post(USER_PATH, 99999L)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue())
            .content(toJson(request));

        // When: Making the request
        final ResultActions response = mockMvc.perform(updateRequest);

        // Then: Should return a BAD_REQUEST error
        response.andExpect(status().is(SC_NOT_FOUND));

        // And: Response should contain the error message
        final String content = response.andReturn().getResponse().getContentAsString();
        final ApiErrorResponseResource error = fromJson(content, ApiErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(ApiErrorCode.USER_NOT_FOUND_ERROR.getReason()));
    }

    @Test
    @DisplayName("Should return error when updating authority without proper authorization")
    public void shouldReturnErrorWhenUpdatingAuthorityWithoutProperAuthorization() throws Exception {
        // Given: A validated user
        final User user = aValidatedUser();

        // And: An authority update request
        final UpdateRoleRequest request = anUpdateRoleRequest(ADMIN);

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
