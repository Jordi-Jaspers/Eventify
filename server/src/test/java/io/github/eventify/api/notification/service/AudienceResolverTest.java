package io.github.eventify.api.notification.service;

import io.github.eventify.api.notification.model.NotificationAudience;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.support.UnitTest;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Audience Resolver")
public class AudienceResolverTest extends UnitTest {

    @Mock
    private UserRepository userRepository;

    private AudienceResolver audienceResolver;

    @BeforeEach
    public void setUp() {
        audienceResolver = new AudienceResolver(userRepository);
    }

    @Test
    @DisplayName("Should return list with single user when user audience resolves to existing user")
    public void shouldResolveUserAudienceToSingleUser() {
        // Given: An existing user
        final User user = aValidUser();
        final NotificationAudience audience = NotificationAudience.user(user.getId());

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        // When: Resolving the audience
        final List<User> result = audienceResolver.resolve(audience);

        // Then: Should return a list with that single user
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), is(user.getId()));
    }

    @Test
    @DisplayName("Should return empty list when user audience resolves to non-existent user")
    public void shouldReturnEmptyListWhenUserNotFound() {
        // Given: A non-existent user ID
        final Long nonExistentUserId = 999L;
        final NotificationAudience audience = NotificationAudience.user(nonExistentUserId);

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When: Resolving the audience
        final List<User> result = audienceResolver.resolve(audience);

        // Then: Should return an empty list (not throw exception)
        assertThat(result, is(empty()));
    }
}
