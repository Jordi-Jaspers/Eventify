package io.github.eventify.api.watchlist.service;

import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.eventify.support.UnitTest;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for WatchlistSecurityService.
 */
@DisplayName("Unit Test - Watchlist Security Service")
class WatchlistSecurityServiceTest extends UnitTest {

    private WatchlistSecurityService securityService;
    private WatchlistRepository watchlistRepository;
    private OrganizationMembershipRepository membershipRepository;

    @BeforeEach
    void setUp() {
        watchlistRepository = mock(WatchlistRepository.class);
        membershipRepository = mock(OrganizationMembershipRepository.class);
        securityService = new WatchlistSecurityService(watchlistRepository, membershipRepository);
    }

    // ========================= USER WATCHLIST TESTS =========================

    @Test
    @DisplayName("Should allow access when user owns watchlist")
    void shouldAllowAccessWhenUserOwnsWatchlist() {
        // Given: User owns watchlist
        final User user = aValidUser();
        user.setId(1L);
        final Watchlist watchlist = aUserWatchlist(1L, user);

        given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));

        // When: Checking access
        final boolean canAccess = securityService.canAccessUserWatchlist(1L, 1L);

        // Then: Should allow access
        assertThat(canAccess, is(true));
    }

    @Test
    @DisplayName("Should deny access when user does not own watchlist")
    void shouldDenyAccessWhenUserDoesNotOwnWatchlist() {
        // Given: Watchlist owned by different user
        final User owner = aValidUser();
        owner.setId(1L);
        final Watchlist watchlist = aUserWatchlist(1L, owner);

        given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));

        // When: Checking access with different user ID
        final boolean canAccess = securityService.canAccessUserWatchlist(1L, 2L);

        // Then: Should deny access
        assertThat(canAccess, is(false));
    }

    @Test
    @DisplayName("Should deny access when watchlist not found")
    void shouldDenyAccessWhenWatchlistNotFound() {
        // Given: Watchlist does not exist
        given(watchlistRepository.findById(999L)).willReturn(Optional.empty());

        // When: Checking access
        final boolean canAccess = securityService.canAccessUserWatchlist(999L, 1L);

        // Then: Should deny access
        assertThat(canAccess, is(false));
    }

    @Test
    @DisplayName("Should deny access when watchlist ID is null")
    void shouldDenyAccessWhenWatchlistIdIsNull() {
        // When: Checking access with null watchlist ID
        final boolean canAccess = securityService.canAccessUserWatchlist(null, 1L);

        // Then: Should deny access
        assertThat(canAccess, is(false));
    }

    @Test
    @DisplayName("Should deny access when user ID is null")
    void shouldDenyAccessWhenUserIdIsNull() {
        // When: Checking access with null user ID
        final boolean canAccess = securityService.canAccessUserWatchlist(1L, null);

        // Then: Should deny access
        assertThat(canAccess, is(false));
    }

    // ========================= ORG WATCHLIST TESTS =========================

    @Test
    @DisplayName("Should allow access when user is org member")
    void shouldAllowAccessWhenUserIsOrgMember() {
        // Given: User is member of organization
        final User user = aValidUser();
        user.setId(1L);
        final Organization org = anOrganization(1L);
        final Watchlist watchlist = anOrgWatchlist(1L, user, org);

        given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
        given(membershipRepository.existsByOrganizationIdAndUserId(1L, 1L)).willReturn(true);

        // When: Checking access
        final boolean canAccess = securityService.canAccessOrgWatchlist(1L, 1L, 1L);

        // Then: Should allow access
        assertThat(canAccess, is(true));
    }

    @Test
    @DisplayName("Should deny access when user is not org member")
    void shouldDenyAccessWhenUserIsNotOrgMember() {
        // Given: User is not member of organization
        final User user = aValidUser();
        user.setId(1L);
        final Organization org = anOrganization(1L);
        final Watchlist watchlist = anOrgWatchlist(1L, user, org);

        given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));
        given(membershipRepository.existsByOrganizationIdAndUserId(1L, 1L)).willReturn(false);

        // When: Checking access
        final boolean canAccess = securityService.canAccessOrgWatchlist(1L, 1L, 1L);

        // Then: Should deny access
        assertThat(canAccess, is(false));
    }

    @Test
    @DisplayName("Should deny access when watchlist belongs to different org")
    void shouldDenyAccessWhenWatchlistBelongsToDifferentOrg() {
        // Given: Watchlist belongs to different organization
        final User user = aValidUser();
        user.setId(1L);
        final Organization org = anOrganization(2L);
        final Watchlist watchlist = anOrgWatchlist(1L, user, org);

        given(watchlistRepository.findById(1L)).willReturn(Optional.of(watchlist));

        // When: Checking access with different org ID
        final boolean canAccess = securityService.canAccessOrgWatchlist(1L, 1L, 1L);

        // Then: Should deny access
        assertThat(canAccess, is(false));
    }

    @Test
    @DisplayName("Should deny access when any parameter is null")
    void shouldDenyAccessWhenAnyParameterIsNull() {
        // When/Then: Should deny access for all null combinations
        assertThat(securityService.canAccessOrgWatchlist(null, 1L, 1L), is(false));
        assertThat(securityService.canAccessOrgWatchlist(1L, null, 1L), is(false));
        assertThat(securityService.canAccessOrgWatchlist(1L, 1L, null), is(false));
    }

    // ========================= HELPER METHODS =========================

    private Watchlist aUserWatchlist(final Long id, final User user) {
        final Watchlist watchlist = new Watchlist("Test Watchlist", user, null);
        watchlist.setId(id);
        return watchlist;
    }

    private Watchlist anOrgWatchlist(final Long id, final User user, final Organization org) {
        final Watchlist watchlist = new Watchlist("Org Watchlist", user, org);
        watchlist.setId(id);
        return watchlist;
    }

    private Organization anOrganization(final Long id) {
        final Organization org = new Organization();
        org.setId(id);
        org.setName("Test Organization");
        return org;
    }
}
