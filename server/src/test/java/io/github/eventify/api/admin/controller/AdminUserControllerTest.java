package io.github.eventify.api.admin.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.response.UserResponse;
import io.github.eventify.support.IntegrationTest;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import tools.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static io.github.eventify.api.Paths.ADMIN_USERS_SEARCH_PATH;
import static io.github.eventify.api.Paths.ADMIN_USER_FORCE_RESET_PATH;
import static io.github.eventify.api.user.model.UserMetaData.SEARCH_TERM;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Admin User Controller - Search Users")
public class AdminUserControllerTest extends IntegrationTest {

    private SortablePageInput createSearchInput(final String query) {
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        if (query != null && !query.isEmpty()) {
            final SearchInput searchInput = new SearchInput();
            searchInput.setFieldName(SEARCH_TERM);
            searchInput.setTextValue(query);
            input.getSearchInputs().add(searchInput);
        }

        return input;
    }

    @Test
    @DisplayName("Should return matching users when searching by email")
    public void searchUsersReturnsMatchingUsersByEmail() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A user with specific email
        final User user = aValidatedUser();

        // When: Searching for users by email
        final String emailQuery = user.getEmail().substring(0, 5);
        final SortablePageInput input = createSearchInput(emailQuery);

        final MockHttpServletRequestBuilder request = post(ADMIN_USERS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(input))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain matching user
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<UserResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource, is(notNullValue()));
        assertThat(pageResource.getContent().size(), is(greaterThan(0)));
    }

    @Test
    @DisplayName("Should return matching users when searching by first name")
    public void searchUsersReturnsMatchingUsersByFirstName() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A user with first name containing search term
        aValidatedUser();

        // When: Searching for users by first name
        final SortablePageInput input = createSearchInput(FIRST_NAME.substring(0, 3));

        final MockHttpServletRequestBuilder request = post(ADMIN_USERS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(input))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain matching user
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<UserResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource, is(notNullValue()));
        assertThat(pageResource.getContent().size(), is(greaterThan(0)));
    }

    @Test
    @DisplayName("Should return matching users when searching by last name")
    public void searchUsersReturnsMatchingUsersByLastName() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A user with last name containing search term
        aValidatedUser();

        // When: Searching for users by last name
        final SortablePageInput input = createSearchInput(LAST_NAME.substring(0, 3));

        final MockHttpServletRequestBuilder request = post(ADMIN_USERS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(input))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain matching user
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<UserResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource, is(notNullValue()));
        assertThat(pageResource.getContent().size(), is(greaterThan(0)));
    }

    @Test
    @DisplayName("Should perform case insensitive search")
    public void searchUsersIsCaseInsensitive() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A user exists
        aValidatedUser();

        // When: Searching with uppercase query
        final SortablePageInput uppercaseInput = createSearchInput(FIRST_NAME.toUpperCase().substring(0, 3));
        final MockHttpServletRequestBuilder uppercaseRequest = post(ADMIN_USERS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(uppercaseInput))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions uppercaseResponse = mockMvc.perform(uppercaseRequest);

        // Then: Response should be OK
        uppercaseResponse.andExpect(status().is(SC_OK));

        // When: Searching with lowercase query
        final SortablePageInput lowercaseInput = createSearchInput(FIRST_NAME.toLowerCase().substring(0, 3));
        final MockHttpServletRequestBuilder lowercaseRequest = post(ADMIN_USERS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(lowercaseInput))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions lowercaseResponse = mockMvc.perform(lowercaseRequest);

        // Then: Response should be OK
        lowercaseResponse.andExpect(status().is(SC_OK));

        // And: Both searches should return results
        final String uppercaseContent = uppercaseResponse.andReturn().getResponse().getContentAsString();
        final PageResource<UserResponse> uppercaseResults = fromJson(uppercaseContent, new TypeReference<>() {});

        final String lowercaseContent = lowercaseResponse.andReturn().getResponse().getContentAsString();
        final PageResource<UserResponse> lowercaseResults = fromJson(lowercaseContent, new TypeReference<>() {});

        assertThat(uppercaseResults.getContent().size(), is(equalTo(lowercaseResults.getContent().size())));
    }

    @Test
    @DisplayName("Should return all users including disabled when no filter applied")
    public void searchUsersReturnsAllUsers() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Some users exist
        aValidatedUser();
        aLockedUser();

        // When: Searching with empty input
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(10);

        final MockHttpServletRequestBuilder request = post(ADMIN_USERS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(input))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain users
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<UserResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getContent(), is(notNullValue()));
        assertThat(pageResource.getContent().size(), is(greaterThan(0)));
    }

    @Test
    @DisplayName("Should return paginated results")
    public void searchUsersReturnsPaginatedResults() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Many users exist
        final List<User> users = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            users.add(aValidatedUser());
        }

        // When: Requesting page with limited size
        final SortablePageInput input = new SortablePageInput();
        input.setPageNumber(0);
        input.setPageSize(5);

        final MockHttpServletRequestBuilder request = post(ADMIN_USERS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(input))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Should return limited results
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<UserResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource.getContent().size(), is(lessThanOrEqualTo(5)));
        assertThat(pageResource.getTotalElements(), is(greaterThanOrEqualTo(15L)));
    }

    @Test
    @DisplayName("Should fail without authentication")
    public void searchUsersWithoutAuthenticationFails() throws Exception {
        // Given: No authentication provided
        final SortablePageInput input = createSearchInput("test");

        // When: Searching without authentication
        final MockHttpServletRequestBuilder request = post(ADMIN_USERS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(input));

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail without required permission")
    public void searchUsersWithoutPermissionFails() throws Exception {
        // Given: A regular user without MANAGE_USERS permission
        final User regularUser = aValidatedUser();
        final SortablePageInput input = createSearchInput("test");

        // When: Regular user attempts to search users
        final MockHttpServletRequestBuilder request = post(ADMIN_USERS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(input))
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return empty page when no matches found")
    public void searchUsersReturnsEmptyListWhenNoMatches() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);
        final SortablePageInput input = createSearchInput("nonexistent987xyz");

        // When: Searching with query that matches no users
        final MockHttpServletRequestBuilder request = post(ADMIN_USERS_SEARCH_PATH)
            .contentType(APPLICATION_JSON)
            .content(toJson(input))
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain empty list or null content
        final String content = response.andReturn().getResponse().getContentAsString();
        final PageResource<UserResponse> pageResource = fromJson(content, new TypeReference<>() {});

        assertThat(pageResource, is(notNullValue()));
        // Content may be null or empty list when no results
        if (pageResource.getContent() != null) {
            assertThat(pageResource.getContent(), hasSize(0));
        }
        assertThat(pageResource.getTotalElements(), is(0L));
    }

    @Test
    @DisplayName("Should successfully trigger password reset for valid user")
    public void forcePasswordResetSuccess() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A target user exists
        final User targetUser = aValidatedUser();

        // When: Admin triggers force password reset
        final MockHttpServletRequestBuilder request = post(
            ADMIN_USER_FORCE_RESET_PATH.replace("{id}", targetUser.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return 404 when user ID does not exist")
    public void forcePasswordResetUserNotFound() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Non-existent user ID
        final Long nonExistentUserId = 999999L;

        // When: Admin triggers force password reset for non-existent user
        final MockHttpServletRequestBuilder request = post(
            ADMIN_USER_FORCE_RESET_PATH.replace("{id}", nonExistentUserId.toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be NOT FOUND
        response.andExpect(status().is(SC_NOT_FOUND));
    }

    @Test
    @DisplayName("Should return 403 when non-admin user attempts force reset")
    public void forcePasswordResetForbiddenForNonAdmin() throws Exception {
        // Given: A regular user without admin privileges
        final User regularUser = aValidatedUser();

        // And: A target user exists
        final User targetUser = aValidatedUser();

        // When: Regular user attempts to trigger force password reset
        final MockHttpServletRequestBuilder request = post(
            ADMIN_USER_FORCE_RESET_PATH.replace("{id}", targetUser.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should successfully trigger reset for locked user")
    public void forcePasswordResetForLockedUserSuccess() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A locked user exists
        final User lockedUser = aLockedUser();

        // When: Admin triggers force password reset for locked user
        final MockHttpServletRequestBuilder request = post(
            ADMIN_USER_FORCE_RESET_PATH.replace("{id}", lockedUser.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should successfully trigger reset for user with unvalidated email")
    public void forcePasswordResetForUnvalidatedUserSuccess() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: An unvalidated user exists
        final User unvalidatedUser = anUnvalidatedUser();

        // When: Admin triggers force password reset for unvalidated user
        final MockHttpServletRequestBuilder request = post(
            ADMIN_USER_FORCE_RESET_PATH.replace("{id}", unvalidatedUser.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));
    }

    @Test
    @DisplayName("Should return 401 when no authentication provided")
    public void forcePasswordResetWithoutAuthenticationFails() throws Exception {
        // Given: A target user exists
        final User targetUser = aValidatedUser();

        // When: Request without authentication
        final MockHttpServletRequestBuilder request = post(
            ADMIN_USER_FORCE_RESET_PATH.replace("{id}", targetUser.getId().toString())
        )
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should invalidate refresh token after force password reset")
    public void forcePasswordResetInvalidatesRefreshToken() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A target user with valid refresh token
        final User targetUser = aValidatedUser();
        final String targetRefreshToken = targetUser.getRefreshToken().getValue();

        // When: Admin triggers force password reset
        final MockHttpServletRequestBuilder forceResetRequest = post(
            ADMIN_USER_FORCE_RESET_PATH.replace("{id}", targetUser.getId().toString())
        )
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        mockMvc.perform(forceResetRequest).andExpect(status().is(SC_OK));

        // Then: Target user's refresh token should be invalidated
        // Attempting to refresh with the old refresh token should fail
        final MockHttpServletRequestBuilder refreshRequest = post("/v1/auth/token")
            .contentType(APPLICATION_JSON)
            .content("{\"refreshToken\":\"" + targetRefreshToken + "\"}");

        final ResultActions response = mockMvc.perform(refreshRequest);

        // And: Should fail because refresh token is now invalid (400 Bad Request)
        response.andExpect(status().is(SC_BAD_REQUEST));
    }
}
