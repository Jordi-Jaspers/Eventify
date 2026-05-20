package io.github.eventify.api.notification.service;

import io.github.eventify.api.authentication.model.Role;
import io.github.eventify.api.notification.model.NotificationAudience;
import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
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

    @Mock
    private OrganizationMembershipRepository organizationMembershipRepository;

    private AudienceResolver audienceResolver;

    @BeforeEach
    public void setUp() {
        audienceResolver = new AudienceResolver(userRepository, organizationMembershipRepository);
    }

    // ========================= USER audience =========================

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

    // ========================= ALL_USERS audience =========================

    @Test
    @DisplayName("Should return all users when audience type is ALL_USERS")
    public void shouldResolveAllUsersAudience() {
        // Given: Multiple users exist
        final User user1 = aValidUser();
        final User user2 = aValidUser();
        user2.setId(2L);
        final NotificationAudience audience = NotificationAudience.allUsers();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // When: Resolving the audience
        final List<User> result = audienceResolver.resolve(audience);

        // Then: Should return all users
        assertThat(result, hasSize(2));
    }

    @Test
    @DisplayName("Should return empty list when no users exist for ALL_USERS audience")
    public void shouldReturnEmptyListWhenNoUsersForAllUsersAudience() {
        // Given: No users exist
        final NotificationAudience audience = NotificationAudience.allUsers();

        when(userRepository.findAll()).thenReturn(List.of());

        // When: Resolving the audience
        final List<User> result = audienceResolver.resolve(audience);

        // Then: Should return empty list
        assertThat(result, is(empty()));
    }

    // ========================= ORGANIZATION audience =========================

    @Test
    @DisplayName("Should return all org members when audience type is ORGANIZATION")
    public void shouldResolveOrganizationAudience() {
        // Given: An organization with two members
        final Long orgId = 10L;
        final User member1 = aValidUser();
        final User member2 = aValidUser();
        member2.setId(2L);

        final OrganizationMembership m1 = aMembership(member1, orgId);
        final OrganizationMembership m2 = aMembership(member2, orgId);

        final NotificationAudience audience = NotificationAudience.organization(orgId);

        when(organizationMembershipRepository.findAllByOrganizationIdWithUser(orgId))
            .thenReturn(List.of(m1, m2));

        // When: Resolving the audience
        final List<User> result = audienceResolver.resolve(audience);

        // Then: Should return both members
        assertThat(result, hasSize(2));
    }

    @Test
    @DisplayName("Should return empty list when organization has no members")
    public void shouldReturnEmptyListWhenOrganizationHasNoMembers() {
        // Given: An organization with no members
        final Long orgId = 10L;
        final NotificationAudience audience = NotificationAudience.organization(orgId);

        when(organizationMembershipRepository.findAllByOrganizationIdWithUser(orgId))
            .thenReturn(List.of());

        // When: Resolving the audience
        final List<User> result = audienceResolver.resolve(audience);

        // Then: Should return empty list
        assertThat(result, is(empty()));
    }

    // ========================= ALL_ORGANIZATION_OWNERS audience =========================

    @Test
    @DisplayName("Should return distinct owners when audience type is ALL_ORGANIZATION_OWNERS")
    public void shouldResolveAllOrganizationOwnersAudience() {
        // Given: Two owners across organizations
        final User owner1 = aValidUser();
        final User owner2 = aValidUser();
        owner2.setId(2L);

        final NotificationAudience audience = NotificationAudience.allOrganizationOwners();

        when(organizationMembershipRepository.findAllOwnersDistinct())
            .thenReturn(List.of(owner1, owner2));

        // When: Resolving the audience
        final List<User> result = audienceResolver.resolve(audience);

        // Then: Should return distinct owners
        assertThat(result, hasSize(2));
    }

    @Test
    @DisplayName("Should return empty list when no organization owners exist")
    public void shouldReturnEmptyListWhenNoOrganizationOwners() {
        // Given: No owners exist
        final NotificationAudience audience = NotificationAudience.allOrganizationOwners();

        when(organizationMembershipRepository.findAllOwnersDistinct())
            .thenReturn(List.of());

        // When: Resolving the audience
        final List<User> result = audienceResolver.resolve(audience);

        // Then: Should return empty list
        assertThat(result, is(empty()));
    }

    // ========================= GLOBAL_ROLE audience =========================

    @Test
    @DisplayName("Should return users with given role when audience type is GLOBAL_ROLE")
    public void shouldResolveGlobalRoleAudience() {
        // Given: Two admin users
        final User admin1 = aValidUser();
        admin1.setRole(Role.ADMIN);
        final User admin2 = aValidUser();
        admin2.setId(2L);
        admin2.setRole(Role.ADMIN);

        final NotificationAudience audience = NotificationAudience.globalRole(Role.ADMIN);

        when(userRepository.findAllByRole(Role.ADMIN)).thenReturn(List.of(admin1, admin2));

        // When: Resolving the audience
        final List<User> result = audienceResolver.resolve(audience);

        // Then: Should return all users with that role
        assertThat(result, hasSize(2));
    }

    @Test
    @DisplayName("Should return empty list when no users have the given role")
    public void shouldReturnEmptyListWhenNoUsersWithRole() {
        // Given: No users with ADMIN role
        final NotificationAudience audience = NotificationAudience.globalRole(Role.ADMIN);

        when(userRepository.findAllByRole(Role.ADMIN)).thenReturn(List.of());

        // When: Resolving the audience
        final List<User> result = audienceResolver.resolve(audience);

        // Then: Should return empty list
        assertThat(result, is(empty()));
    }

    // ========================= count() =========================

    @Test
    @DisplayName("Should return count of all users when audience type is ALL_USERS")
    public void shouldCountAllUsersAudience() {
        // Given: 5 users exist
        final NotificationAudience audience = NotificationAudience.allUsers();

        when(userRepository.count()).thenReturn(5L);

        // When: Counting the audience
        final long count = audienceResolver.count(audience);

        // Then: Should return 5
        assertThat(count, is(5L));
    }

    @Test
    @DisplayName("Should return 1 when counting single user audience and user exists")
    public void shouldCountSingleUserAudience() {
        // Given: An existing user
        final User user = aValidUser();
        final NotificationAudience audience = NotificationAudience.user(user.getId());

        when(userRepository.existsById(user.getId())).thenReturn(true);

        // When: Counting the audience
        final long count = audienceResolver.count(audience);

        // Then: Should return 1
        assertThat(count, is(1L));
    }

    @Test
    @DisplayName("Should return 0 when counting single user audience and user does not exist")
    public void shouldReturnZeroCountWhenUserNotFound() {
        // Given: A non-existent user
        final Long nonExistentId = 999L;
        final NotificationAudience audience = NotificationAudience.user(nonExistentId);

        when(userRepository.existsById(nonExistentId)).thenReturn(false);

        // When: Counting the audience
        final long count = audienceResolver.count(audience);

        // Then: Should return 0
        assertThat(count, is(0L));
    }

    @Test
    @DisplayName("Should return member count when counting organization audience")
    public void shouldCountOrganizationAudience() {
        // Given: An organization with 3 members
        final Long orgId = 10L;
        final NotificationAudience audience = NotificationAudience.organization(orgId);

        when(organizationMembershipRepository.countByOrganizationId(orgId)).thenReturn(3L);

        // When: Counting the audience
        final long count = audienceResolver.count(audience);

        // Then: Should return 3
        assertThat(count, is(3L));
    }

    @Test
    @DisplayName("Should return distinct owner count when counting ALL_ORGANIZATION_OWNERS audience")
    public void shouldCountAllOrganizationOwnersAudience() {
        // Given: 4 distinct owners
        final NotificationAudience audience = NotificationAudience.allOrganizationOwners();

        when(organizationMembershipRepository.countDistinctOwners()).thenReturn(4L);

        // When: Counting the audience
        final long count = audienceResolver.count(audience);

        // Then: Should return 4
        assertThat(count, is(4L));
    }

    @Test
    @DisplayName("Should return role count when counting GLOBAL_ROLE audience")
    public void shouldCountGlobalRoleAudience() {
        // Given: 2 users with USER role
        final NotificationAudience audience = NotificationAudience.globalRole(Role.USER);

        when(userRepository.countByRole(Role.USER)).thenReturn(2L);

        // When: Counting the audience
        final long count = audienceResolver.count(audience);

        // Then: Should return 2
        assertThat(count, is(2L));
    }

    // ========================= FACTORY METHODS =========================

    private static OrganizationMembership aMembership(final User user, final Long orgId) {
        final OrganizationMembership membership = new OrganizationMembership();
        membership.setUser(user);
        membership.setRole(OrganizationalRole.MEMBER);
        return membership;
    }
}
