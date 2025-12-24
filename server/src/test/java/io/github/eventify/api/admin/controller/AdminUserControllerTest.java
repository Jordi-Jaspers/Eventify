package io.github.eventify.api.admin.controller;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.response.UserSearchResult;
import io.github.eventify.support.IntegrationTest;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.core.type.TypeReference;

import static io.github.eventify.api.Paths.ADMIN_USERS_SEARCH_PATH;
import static io.github.eventify.common.constant.Constants.Security.BEARER;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static jakarta.servlet.http.HttpServletResponse.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integration Test - Admin User Controller")
public class AdminUserControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return matching users when searching by email")
    public void searchUsersReturnsMatchingUsersByEmail() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A user with specific email containing "john"
        final User johnUser = aValidatedUser();

        // When: Searching for users by email containing "john"
        final String emailQuery = johnUser.getEmail().substring(0, 5);
        final MockHttpServletRequestBuilder request = get(ADMIN_USERS_SEARCH_PATH)
            .param("query", emailQuery)
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain matching user
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<UserSearchResult> results = fromJson(content, new TypeReference<>() {});

        assertThat(results, is(notNullValue()));
        assertThat(results.size(), is(greaterThan(0)));
    }

    @Test
    @DisplayName("Should return matching users when searching by first name")
    public void searchUsersReturnsMatchingUsersByFirstName() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A user with first name containing search term
        final User user = aValidatedUser();

        // When: Searching for users by first name
        final MockHttpServletRequestBuilder request = get(ADMIN_USERS_SEARCH_PATH)
            .param("query", FIRST_NAME.substring(0, 3))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain matching user
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<UserSearchResult> results = fromJson(content, new TypeReference<>() {});

        assertThat(results, is(notNullValue()));
        assertThat(results.size(), is(greaterThan(0)));
        assertThat(results.get(0).getFirstName(), containsString(FIRST_NAME.substring(0, 3)));
    }

    @Test
    @DisplayName("Should return matching users when searching by last name")
    public void searchUsersReturnsMatchingUsersByLastName() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A user with last name containing search term
        final User user = aValidatedUser();

        // When: Searching for users by last name
        final MockHttpServletRequestBuilder request = get(ADMIN_USERS_SEARCH_PATH)
            .param("query", LAST_NAME.substring(0, 3))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain matching user
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<UserSearchResult> results = fromJson(content, new TypeReference<>() {});

        assertThat(results, is(notNullValue()));
        assertThat(results.size(), is(greaterThan(0)));
        assertThat(results.get(0).getLastName(), containsString(LAST_NAME.substring(0, 3)));
    }

    @Test
    @DisplayName("Should perform case insensitive search")
    public void searchUsersIsCaseInsensitive() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A user exists
        final User user = aValidatedUser();

        // When: Searching with uppercase query
        final MockHttpServletRequestBuilder uppercaseRequest = get(ADMIN_USERS_SEARCH_PATH)
            .param("query", FIRST_NAME.toUpperCase().substring(0, 3))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions uppercaseResponse = mockMvc.perform(uppercaseRequest);

        // Then: Response should be OK
        uppercaseResponse.andExpect(status().is(SC_OK));

        // When: Searching with lowercase query
        final MockHttpServletRequestBuilder lowercaseRequest = get(ADMIN_USERS_SEARCH_PATH)
            .param("query", FIRST_NAME.toLowerCase().substring(0, 3))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions lowercaseResponse = mockMvc.perform(lowercaseRequest);

        // Then: Response should be OK
        lowercaseResponse.andExpect(status().is(SC_OK));

        // And: Both searches should return same results
        final String uppercaseContent = uppercaseResponse.andReturn().getResponse().getContentAsString();
        final List<UserSearchResult> uppercaseResults = fromJson(uppercaseContent, new TypeReference<>() {});

        final String lowercaseContent = lowercaseResponse.andReturn().getResponse().getContentAsString();
        final List<UserSearchResult> lowercaseResults = fromJson(lowercaseContent, new TypeReference<>() {});

        assertThat(uppercaseResults.size(), is(equalTo(lowercaseResults.size())));
    }

    @Test
    @DisplayName("Should exclude disabled users from search results")
    public void searchUsersExcludesDisabledUsers() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: A disabled user
        final User disabledUser = aLockedUser();

        // When: Searching for the disabled user by email
        final MockHttpServletRequestBuilder request = get(ADMIN_USERS_SEARCH_PATH)
            .param("query", disabledUser.getEmail().substring(0, 5))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Disabled user should not be in results
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<UserSearchResult> results = fromJson(content, new TypeReference<>() {});

        final boolean disabledUserInResults = results.stream()
            .anyMatch(result -> result.getEmail().equals(disabledUser.getEmail()));

        assertThat(disabledUserInResults, is(false));
    }

    @Test
    @DisplayName("Should return maximum of ten results")
    public void searchUsersReturnsMaxTenResults() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: More than 10 users exist
        final List<User> users = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            users.add(aValidatedUser());
        }

        // When: Searching with broad query that matches many users
        final MockHttpServletRequestBuilder request = get(ADMIN_USERS_SEARCH_PATH)
            .param("query", TEST_EMAIL.substring(0, 5))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Should return maximum 10 results
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<UserSearchResult> results = fromJson(content, new TypeReference<>() {});

        assertThat(results.size(), is(lessThanOrEqualTo(10)));
    }

    @Test
    @DisplayName("Should fail when query is less than three characters")
    public void searchUsersWithQueryLessThanThreeCharsFails() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // When: Searching with query less than 3 characters
        final MockHttpServletRequestBuilder request = get(ADMIN_USERS_SEARCH_PATH)
            .param("query", "ab")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail when query is empty")
    public void searchUsersWithEmptyQueryFails() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // When: Searching with empty query
        final MockHttpServletRequestBuilder request = get(ADMIN_USERS_SEARCH_PATH)
            .param("query", "")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be BAD_REQUEST
        response.andExpect(status().is(SC_BAD_REQUEST));
    }

    @Test
    @DisplayName("Should fail without authentication")
    public void searchUsersWithoutAuthenticationFails() throws Exception {
        // Given: No authentication provided

        // When: Searching without authentication
        final MockHttpServletRequestBuilder request = get(ADMIN_USERS_SEARCH_PATH)
            .param("query", "test")
            .contentType(APPLICATION_JSON);

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be UNAUTHORIZED
        response.andExpect(status().is(SC_UNAUTHORIZED));
    }

    @Test
    @DisplayName("Should fail without required permission")
    public void searchUsersWithoutPermissionFails() throws Exception {
        // Given: A regular user without PROVISION_ORGANIZATIONS permission
        final User regularUser = aValidatedUser();

        // When: Regular user attempts to search users
        final MockHttpServletRequestBuilder request = get(ADMIN_USERS_SEARCH_PATH)
            .param("query", "test")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + regularUser.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be FORBIDDEN
        response.andExpect(status().is(SC_FORBIDDEN));
    }

    @Test
    @DisplayName("Should return empty list when no matches found")
    public void searchUsersReturnsEmptyListWhenNoMatches() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // When: Searching with query that matches no users
        final MockHttpServletRequestBuilder request = get(ADMIN_USERS_SEARCH_PATH)
            .param("query", "nonexistent987xyz")
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Response should contain empty list
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<UserSearchResult> results = fromJson(content, new TypeReference<>() {});

        assertThat(results, is(notNullValue()));
        assertThat(results, hasSize(0));
    }

    @Test
    @DisplayName("Should return results sorted alphabetically by email")
    public void searchUsersReturnsSortedByEmail() throws Exception {
        // Given: An admin user
        final User admin = aValidatedUserWithRole(Role.ADMIN);

        // And: Multiple users exist
        aValidatedUser();
        aValidatedUser();
        aValidatedUser();

        // When: Searching with broad query
        final MockHttpServletRequestBuilder request = get(ADMIN_USERS_SEARCH_PATH)
            .param("query", TEST_EMAIL.substring(0, 5))
            .contentType(APPLICATION_JSON)
            .header(AUTHORIZATION, BEARER + admin.getAccessToken().getValue());

        final ResultActions response = mockMvc.perform(request);

        // Then: Response should be OK
        response.andExpect(status().is(SC_OK));

        // And: Results should be sorted by email
        final String content = response.andReturn().getResponse().getContentAsString();
        final List<UserSearchResult> results = fromJson(content, new TypeReference<>() {});

        assertThat(results, is(notNullValue()));

        // Verify sorting
        for (int i = 0; i < results.size() - 1; i++) {
            final String currentEmail = results.get(i).getEmail();
            final String nextEmail = results.get(i + 1).getEmail();
            assertThat(currentEmail.compareTo(nextEmail), is(lessThanOrEqualTo(0)));
        }
    }
}
