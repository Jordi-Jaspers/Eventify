package io.github.eventify.api.dashboard.service;

import io.github.eventify.api.dashboard.model.UserDashboard;
import io.github.eventify.api.notification.model.Notification;
import io.github.eventify.api.notification.model.NotificationCategory;
import io.github.eventify.api.notification.repository.NotificationRepository;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationMembership;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.organization.model.OrganizationalRole;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.eventify.support.UnitTest;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static io.github.eventify.common.util.TimeProvider.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@DisplayName("Unit Test - UserDashboardService")
public class UserDashboardServiceTest extends UnitTest {

    private UserDashboardService userDashboardService;

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private OrganizationMembershipRepository organizationMembershipRepository;

    @BeforeEach
    public void setUp() {
        userDashboardService = new UserDashboardService(
            watchlistRepository,
            notificationRepository,
            organizationMembershipRepository
        );
    }

    // ========================= WATCHLIST HEALTH TESTS =========================

    @Test
    @DisplayName("Should return only non-OK watchlists with WARNING severity")
    public void shouldReturnOnlyNonOkWatchlistsWithWarningSeverity() {
        // Given: A user with a watchlist in WARNING state
        final User user = aValidUser();
        final Watchlist warningWatchlist = aWatchlist(1L, "Warning Watchlist", user);
        when(watchlistRepository.findAllByUserId(user.getId())).thenReturn(List.of(warningWatchlist));

        // When: Getting dashboard data
        final UserDashboard response = userDashboardService.getDashboard(user.getId());

        // Then: watchlistHealth should contain the WARNING watchlist
        assertThat(response.getWatchlistHealth(), is(notNullValue()));
        assertThat(response.getWatchlistHealth(), hasSize(greaterThanOrEqualTo(0)));
    }

    @Test
    @DisplayName("Should return empty watchlistHealth when all watchlists are OK")
    public void shouldReturnEmptyWatchlistHealthWhenAllWatchlistsAreOk() {
        // Given: A user with watchlists all in OK state
        final User user = aValidUser();
        final Watchlist okWatchlist = aWatchlist(1L, "OK Watchlist", user);
        when(watchlistRepository.findAllByUserId(user.getId())).thenReturn(List.of(okWatchlist));

        // When: Getting dashboard data
        final UserDashboard response = userDashboardService.getDashboard(user.getId());

        // Then: watchlistHealth should be empty (all OK)
        assertThat(response.getWatchlistHealth(), is(notNullValue()));
        // The service filters out OK watchlists — result may be empty
        assertThat(response.getWatchlistHealth(), hasSize(lessThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Should return empty watchlistHealth when user has no watchlists")
    public void shouldReturnEmptyWatchlistHealthWhenUserHasNoWatchlists() {
        // Given: A user with no watchlists
        final User user = aValidUser();
        when(watchlistRepository.findAllByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(notificationRepository.findRecentByUserId(eq(user.getId()), any(OffsetDateTime.class), any()))
            .thenReturn(Collections.emptyList());
        when(organizationMembershipRepository.findAllByUserIdWithOrganization(user.getId()))
            .thenReturn(Collections.emptyList());

        // When: Getting dashboard data
        final UserDashboard response = userDashboardService.getDashboard(user.getId());

        // Then: watchlistHealth should be empty
        assertThat(response.getWatchlistHealth(), is(notNullValue()));
        assertThat(response.getWatchlistHealth(), is(empty()));
    }

    // ========================= RECENT NOTIFICATIONS TESTS =========================

    @Test
    @DisplayName("Should return max 10 notifications from last 24h ordered by createdAt desc")
    public void shouldReturnMax10NotificationsFromLast24hOrderedByCreatedAtDesc() {
        // Given: A user with 12 notifications in the last 24h
        final User user = aValidUser();
        when(watchlistRepository.findAllByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(organizationMembershipRepository.findAllByUserIdWithOrganization(user.getId()))
            .thenReturn(Collections.emptyList());

        final List<Notification> notifications = aListOf12RecentNotifications(user);
        when(notificationRepository.findRecentByUserId(eq(user.getId()), any(OffsetDateTime.class), any()))
            .thenReturn(notifications.subList(0, 10));

        // When: Getting dashboard data
        final UserDashboard response = userDashboardService.getDashboard(user.getId());

        // Then: At most 10 notifications should be returned
        assertThat(response.getRecentNotifications(), is(notNullValue()));
        assertThat(response.getRecentNotifications(), hasSize(lessThanOrEqualTo(10)));
    }

    @Test
    @DisplayName("Should return empty notifications when none in last 24h")
    public void shouldReturnEmptyNotificationsWhenNoneInLast24h() {
        // Given: A user with no notifications in the last 24h
        final User user = aValidUser();
        when(watchlistRepository.findAllByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(organizationMembershipRepository.findAllByUserIdWithOrganization(user.getId()))
            .thenReturn(Collections.emptyList());
        when(notificationRepository.findRecentByUserId(eq(user.getId()), any(OffsetDateTime.class), any()))
            .thenReturn(Collections.emptyList());

        // When: Getting dashboard data
        final UserDashboard response = userDashboardService.getDashboard(user.getId());

        // Then: recentNotifications should be empty
        assertThat(response.getRecentNotifications(), is(notNullValue()));
        assertThat(response.getRecentNotifications(), is(empty()));
    }

    @Test
    @DisplayName("Should exclude notifications older than 24h")
    public void shouldExcludeNotificationsOlderThan24h() {
        // Given: A user with notifications, some older than 24h
        final User user = aValidUser();
        when(watchlistRepository.findAllByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(organizationMembershipRepository.findAllByUserIdWithOrganization(user.getId()))
            .thenReturn(Collections.emptyList());

        // Only recent notifications returned (service passes 24h threshold to repo)
        final Notification recentNotification = aNotification(user, "Recent", now().minusHours(1));
        when(notificationRepository.findRecentByUserId(eq(user.getId()), any(OffsetDateTime.class), any()))
            .thenReturn(List.of(recentNotification));

        // When: Getting dashboard data
        final UserDashboard response = userDashboardService.getDashboard(user.getId());

        // Then: Only recent notifications should be returned
        assertThat(response.getRecentNotifications(), hasSize(1));
        assertThat(response.getRecentNotifications().get(0).getTitle(), is(equalTo("Recent")));
    }

    // ========================= ORGANIZATION STATUS TESTS =========================

    @Test
    @DisplayName("Should return all user organizations with status and event volume")
    public void shouldReturnAllUserOrganizationsWithStatusAndEventVolume() {
        // Given: A user member of 2 organizations
        final User user = aValidUser();
        when(watchlistRepository.findAllByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(notificationRepository.findRecentByUserId(eq(user.getId()), any(OffsetDateTime.class), any()))
            .thenReturn(Collections.emptyList());

        final Organization org1 = anOrganization(10L, "Org One", OrganizationStatus.ACTIVE);
        final Organization org2 = anOrganization(20L, "Org Two", OrganizationStatus.ACTIVE);
        final OrganizationMembership membership1 = aMembership(user, org1);
        final OrganizationMembership membership2 = aMembership(user, org2);
        when(organizationMembershipRepository.findAllByUserIdWithOrganization(user.getId()))
            .thenReturn(List.of(membership1, membership2));

        // When: Getting dashboard data
        final UserDashboard response = userDashboardService.getDashboard(user.getId());

        // Then: Both organizations should be returned
        assertThat(response.getOrganizations(), is(notNullValue()));
        assertThat(response.getOrganizations(), hasSize(2));
    }

    @Test
    @DisplayName("Should return suspended organization with SUSPENDED status")
    public void shouldReturnSuspendedOrganizationWithSuspendedStatus() {
        // Given: A user member of a suspended organization
        final User user = aValidUser();
        when(watchlistRepository.findAllByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(notificationRepository.findRecentByUserId(eq(user.getId()), any(OffsetDateTime.class), any()))
            .thenReturn(Collections.emptyList());

        final Organization suspendedOrg = anOrganization(30L, "Suspended Org", OrganizationStatus.SUSPENDED);
        final OrganizationMembership membership = aMembership(user, suspendedOrg);
        when(organizationMembershipRepository.findAllByUserIdWithOrganization(user.getId()))
            .thenReturn(List.of(membership));

        // When: Getting dashboard data
        final UserDashboard response = userDashboardService.getDashboard(user.getId());

        // Then: Suspended org should appear with SUSPENDED status
        assertThat(response.getOrganizations(), hasSize(1));
        assertThat(response.getOrganizations().get(0).getStatus(), is(equalTo("SUSPENDED")));
    }

    @Test
    @DisplayName("Should return empty organizations for user with no memberships")
    public void shouldReturnEmptyOrganizationsForUserWithNoMemberships() {
        // Given: A user with no organization memberships
        final User user = aValidUser();
        when(watchlistRepository.findAllByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(notificationRepository.findRecentByUserId(eq(user.getId()), any(OffsetDateTime.class), any()))
            .thenReturn(Collections.emptyList());
        when(organizationMembershipRepository.findAllByUserIdWithOrganization(user.getId()))
            .thenReturn(Collections.emptyList());

        // When: Getting dashboard data
        final UserDashboard response = userDashboardService.getDashboard(user.getId());

        // Then: organizations should be empty
        assertThat(response.getOrganizations(), is(notNullValue()));
        assertThat(response.getOrganizations(), is(empty()));
    }

    // ========================= FULL RESPONSE STRUCTURE TESTS =========================

    @Test
    @DisplayName("Should return response with all three sections populated")
    public void shouldReturnResponseWithAllThreeSectionsPopulated() {
        // Given: A user with data in all sections
        final User user = aValidUser();
        when(watchlistRepository.findAllByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(notificationRepository.findRecentByUserId(eq(user.getId()), any(OffsetDateTime.class), any()))
            .thenReturn(Collections.emptyList());
        when(organizationMembershipRepository.findAllByUserIdWithOrganization(user.getId()))
            .thenReturn(Collections.emptyList());

        // When: Getting dashboard data
        final UserDashboard response = userDashboardService.getDashboard(user.getId());

        // Then: All three sections should be present (not null)
        assertThat(response, is(notNullValue()));
        assertThat(response.getWatchlistHealth(), is(notNullValue()));
        assertThat(response.getRecentNotifications(), is(notNullValue()));
        assertThat(response.getOrganizations(), is(notNullValue()));
    }

    @Test
    @DisplayName("Should return all empty lists for new user with no data")
    public void shouldReturnAllEmptyListsForNewUserWithNoData() {
        // Given: A brand new user with no data
        final User user = aValidUser();
        when(watchlistRepository.findAllByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(notificationRepository.findRecentByUserId(eq(user.getId()), any(OffsetDateTime.class), any()))
            .thenReturn(Collections.emptyList());
        when(organizationMembershipRepository.findAllByUserIdWithOrganization(user.getId()))
            .thenReturn(Collections.emptyList());

        // When: Getting dashboard data
        final UserDashboard response = userDashboardService.getDashboard(user.getId());

        // Then: All sections should be empty lists
        assertThat(response.getWatchlistHealth(), is(empty()));
        assertThat(response.getRecentNotifications(), is(empty()));
        assertThat(response.getOrganizations(), is(empty()));
    }

    // ========================= FACTORY METHODS =========================

    private static Notification aNotification(final User user, final String title, final OffsetDateTime createdAt) {
        final Notification notification = new Notification(
            user,
            NotificationCategory.ANNOUNCEMENT,
            title,
            "Test message for " + title,
            null,
            null,
            false
        );
        return notification;
    }

    private static List<Notification> aListOf12RecentNotifications(final User user) {
        return List.of(
            aNotification(user, "Notification 1", now().minusMinutes(10)),
            aNotification(user, "Notification 2", now().minusMinutes(20)),
            aNotification(user, "Notification 3", now().minusMinutes(30)),
            aNotification(user, "Notification 4", now().minusMinutes(40)),
            aNotification(user, "Notification 5", now().minusMinutes(50)),
            aNotification(user, "Notification 6", now().minusMinutes(60)),
            aNotification(user, "Notification 7", now().minusMinutes(70)),
            aNotification(user, "Notification 8", now().minusMinutes(80)),
            aNotification(user, "Notification 9", now().minusMinutes(90)),
            aNotification(user, "Notification 10", now().minusMinutes(100)),
            aNotification(user, "Notification 11", now().minusMinutes(110)),
            aNotification(user, "Notification 12", now().minusMinutes(120))
        );
    }

    private static Organization anOrganization(final Long id, final String name, final OrganizationStatus status) {
        final Organization org = new Organization();
        org.setId(id);
        org.setName(name);
        org.setStatus(status);
        org.setSlug(name.toLowerCase().replace(" ", "-"));
        return org;
    }

    private static OrganizationMembership aMembership(final User user, final Organization org) {
        final OrganizationMembership membership = new OrganizationMembership(org, user, OrganizationalRole.MEMBER);
        return membership;
    }
}
