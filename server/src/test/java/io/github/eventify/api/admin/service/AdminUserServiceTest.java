package io.github.eventify.api.admin.service;

import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.response.UserSearchResult;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.support.UnitTest;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - Admin User Service")
public class AdminUserServiceTest extends UnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    @BeforeEach
    public void setUp() {
        adminUserService = new AdminUserService(userRepository);
    }

    @Test
    @DisplayName("Should search users with valid query")
    public void shouldSearchUsersWithValidQuery() {
        // Given: A valid search query
        final String query = "john";
        final User user = aValidUser();
        when(userRepository.searchUsers(anyString(), any(Pageable.class)))
            .thenReturn(List.of(user));

        // When: Searching for users
        final List<UserSearchResult> results = adminUserService.searchUsers(query);

        // Then: Repository should be called with correct query and pageable
        verify(userRepository).searchUsers(anyString(), any(Pageable.class));

        // And: Results should be returned
        assertThat(results, is(notNullValue()));
        assertThat(results.size(), is(greaterThan(0)));
    }

    @Test
    @DisplayName("Should return mapped UserSearchResult DTOs")
    public void shouldReturnMappedUserSearchResults() {
        // Given: Valid users in repository
        final User user1 = aValidUser();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setFirstName("John");
        user1.setLastName("Doe");

        final User user2 = aValidUser();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");

        when(userRepository.searchUsers(anyString(), any(Pageable.class)))
            .thenReturn(List.of(user1, user2));

        // When: Searching for users
        final List<UserSearchResult> results = adminUserService.searchUsers("test");

        // Then: Results should be correctly mapped to DTOs
        assertThat(results, is(notNullValue()));
        assertThat(results, hasSize(2));

        final UserSearchResult result1 = results.get(0);
        assertThat(result1.getId(), is(equalTo(1L)));
        assertThat(result1.getEmail(), is(equalTo("user1@example.com")));
        assertThat(result1.getFirstName(), is(equalTo("John")));
        assertThat(result1.getLastName(), is(equalTo("Doe")));

        final UserSearchResult result2 = results.get(1);
        assertThat(result2.getId(), is(equalTo(2L)));
        assertThat(result2.getEmail(), is(equalTo("user2@example.com")));
        assertThat(result2.getFirstName(), is(equalTo("Jane")));
        assertThat(result2.getLastName(), is(equalTo("Smith")));
    }

    @Test
    @DisplayName("Should return empty list when no users found")
    public void shouldReturnEmptyListWhenNoUsersFound() {
        // Given: No users match the search query
        when(userRepository.searchUsers(anyString(), any(Pageable.class)))
            .thenReturn(Collections.emptyList());

        // When: Searching for users
        final List<UserSearchResult> results = adminUserService.searchUsers("nonexistent");

        // Then: Empty list should be returned
        assertThat(results, is(notNullValue()));
        assertThat(results, hasSize(0));
    }

    @Test
    @DisplayName("Should throw exception when query is too short")
    public void shouldThrowExceptionWhenQueryTooShort() {
        // Given: A query with less than 3 characters
        final String shortQuery = "ab";

        // When & Then: Should throw validation exception
        try {
            adminUserService.searchUsers(shortQuery);
            assertThat("Expected exception to be thrown", false);
        } catch (final Exception e) {
            assertThat(e, is(instanceOf(IllegalArgumentException.class)));
        }
    }
}
