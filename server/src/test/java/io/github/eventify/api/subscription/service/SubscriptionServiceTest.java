package io.github.eventify.api.subscription.service;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.model.OrganizationStatus;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.subscription.model.Subscription;
import io.github.eventify.api.subscription.model.request.CreateSubscriptionRequest;
import io.github.eventify.api.subscription.model.request.UpdateSubscriptionRequest;
import io.github.eventify.api.subscription.repository.SubscriptionRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.eventify.common.security.SecurityUtil;
import io.github.eventify.support.UnitTest;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.DataNotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Subscription Service")
public class SubscriptionServiceTest extends UnitTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Spy
    private SubscriptionMetaData subscriptionMetaData = new SubscriptionMetaData();

    @InjectMocks
    private SubscriptionService subscriptionService;

    private MockedStatic<SecurityUtil> securityUtilMock;
    private User user;

    @BeforeEach
    public void setUp() {
        user = aValidUser();
        user.setId(1L);

        securityUtilMock = mockStatic(SecurityUtil.class);
        securityUtilMock.when(SecurityUtil::getLoggedInUser).thenReturn(user);
    }

    @AfterEach
    public void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
    }

    // ========================= createPersonalSubscription =========================

    @Test
    @DisplayName("Should create personal subscription successfully")
    public void shouldCreatePersonalSubscriptionSuccessfully() {
        // Given: an existing watchlist
        final Watchlist watchlist = aWatchlist(10L, "My Watchlist", user);
        when(watchlistRepository.findById(10L)).thenReturn(Optional.of(watchlist));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> {
            final Subscription sub = inv.getArgument(0);
            sub.setId(1L);
            return sub;
        });

        // When: creating a personal subscription
        final CreateSubscriptionRequest request = aValidCreateRequest(10L);
        final Subscription result = subscriptionService.createPersonalSubscription(request);

        // Then: subscription is created with correct user and no organization
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(1L));
        assertThat(result.getUser(), is(user));
        assertThat(result.getOrganization(), is(nullValue()));
        assertThat(result.getTargetSeverities(), hasItems(Severity.CRITICAL));
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when watchlist does not exist on create personal")
    public void shouldThrowWhenWatchlistNotFoundOnCreatePersonal() {
        // Given: watchlist does not exist
        when(watchlistRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then: DataNotFoundException is thrown
        assertThrows(
            DataNotFoundException.class,
            () -> subscriptionService.createPersonalSubscription(aValidCreateRequest(99L))
        );
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when subscribing to watchlist of suspended org")
    public void shouldThrowWhenWatchlistBelongsToSuspendedOrg() {
        // Given: watchlist belongs to a suspended organization
        final Organization suspendedOrg = anOrganization(5L, OrganizationStatus.SUSPENDED);
        final Watchlist watchlist = aWatchlistWithOrg(10L, user, suspendedOrg);
        when(watchlistRepository.findById(10L)).thenReturn(Optional.of(watchlist));

        // When / Then: forbidden exception is thrown
        final RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> subscriptionService.createPersonalSubscription(aValidCreateRequest(10L))
        );

        assertThat(ex, is(notNullValue()));
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should upsert when personal subscription already exists for same user and watchlist")
    public void shouldUpsertWhenPersonalSubscriptionAlreadyExists() {
        // Given: an existing subscription for the same user and watchlist
        final Watchlist watchlist = aWatchlist(10L, "My Watchlist", user);
        final Subscription existing = aSubscription(1L, watchlist, user, null);
        when(watchlistRepository.findById(10L)).thenReturn(Optional.of(watchlist));
        when(subscriptionRepository.findByWatchlistIdAndUserIdAndOrganizationIsNull(10L, user.getId()))
            .thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        // When: creating the same personal subscription again
        final CreateSubscriptionRequest request = aValidCreateRequest(10L);
        final Subscription result = subscriptionService.createPersonalSubscription(request);

        // Then: existing subscription is updated (upsert)
        assertThat(result.getId(), is(1L));
        verify(subscriptionRepository).save(existing);
    }

    // ========================= updatePersonalSubscription =========================

    @Test
    @DisplayName("Should update personal subscription successfully")
    public void shouldUpdatePersonalSubscriptionSuccessfully() {
        // Given: an existing personal subscription owned by the current user
        final Watchlist watchlist = aWatchlist(10L, "My Watchlist", user);
        final Subscription existing = aSubscription(1L, watchlist, user, null);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        // When: updating the subscription
        final UpdateSubscriptionRequest request = aValidUpdateRequest();
        final Subscription result = subscriptionService.updatePersonalSubscription(1L, request);

        // Then: subscription is updated
        assertThat(result.getId(), is(1L));
        assertThat(result.getTargetSeverities(), hasItems(Severity.WARNING));
        verify(subscriptionRepository).save(existing);
    }

    @Test
    @DisplayName("Should throw exception when updating a subscription owned by a different user")
    public void shouldThrowWhenUpdatingSubscriptionOwnedByDifferentUser() {
        // Given: a subscription belonging to another user
        final User otherUser = aValidUser();
        otherUser.setId(99L);
        final Watchlist watchlist = aWatchlist(10L, "Others Watchlist", otherUser);
        final Subscription existing = aSubscription(1L, watchlist, otherUser, null);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(existing));

        // When / Then: forbidden exception is thrown
        assertThrows(
            RuntimeException.class,
            () -> subscriptionService.updatePersonalSubscription(1L, aValidUpdateRequest())
        );
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when subscription to update does not exist")
    public void shouldThrowWhenPersonalSubscriptionToUpdateNotFound() {
        // Given: subscription does not exist
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        // When / Then: DataNotFoundException is thrown
        assertThrows(
            DataNotFoundException.class,
            () -> subscriptionService.updatePersonalSubscription(999L, aValidUpdateRequest())
        );
    }

    // ========================= deletePersonalSubscription =========================

    @Test
    @DisplayName("Should delete personal subscription successfully")
    public void shouldDeletePersonalSubscriptionSuccessfully() {
        // Given: an existing personal subscription owned by the current user
        final Watchlist watchlist = aWatchlist(10L, "My Watchlist", user);
        final Subscription existing = aSubscription(1L, watchlist, user, null);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(existing));

        // When: deleting the personal subscription
        subscriptionService.deletePersonalSubscription(1L);

        // Then: subscription is deleted
        verify(subscriptionRepository).delete(existing);
    }

    @Test
    @DisplayName("Should throw exception when deleting subscription owned by different user")
    public void shouldThrowWhenDeletingSubscriptionOwnedByDifferentUser() {
        // Given: a subscription owned by another user
        final User otherUser = aValidUser();
        otherUser.setId(99L);
        final Watchlist watchlist = aWatchlist(10L, "Others Watchlist", otherUser);
        final Subscription existing = aSubscription(1L, watchlist, otherUser, null);
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(existing));

        // When / Then: forbidden exception is thrown
        assertThrows(
            RuntimeException.class,
            () -> subscriptionService.deletePersonalSubscription(1L)
        );
        verify(subscriptionRepository, never()).delete(any(Subscription.class));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when personal subscription to delete does not exist")
    public void shouldThrowWhenPersonalSubscriptionToDeleteNotFound() {
        // Given: subscription does not exist
        when(subscriptionRepository.findById(999L)).thenReturn(Optional.empty());

        // When / Then: DataNotFoundException is thrown
        assertThrows(
            DataNotFoundException.class,
            () -> subscriptionService.deletePersonalSubscription(999L)
        );
        verify(subscriptionRepository, never()).delete(any(Subscription.class));
    }

    // ========================= searchPersonalSubscriptions =========================

    @Test
    @DisplayName("Should return paginated personal subscriptions for current user")
    public void shouldReturnPaginatedPersonalSubscriptions() {
        // Given: subscriptions exist for the current user
        final Watchlist watchlist = aWatchlist(10L, "My Watchlist", user);
        final Subscription sub = aSubscription(1L, watchlist, user, null);
        final Page<Subscription> page = new PageImpl<>(List.of(sub));
        when(subscriptionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // When: searching personal subscriptions
        final Page<Subscription> result = subscriptionService.searchPersonalSubscriptions(new SortablePageInput());

        // Then: page with user's subscriptions is returned
        assertThat(result.getTotalElements(), is(1L));
        assertThat(result.getContent(), hasSize(1));
    }

    // ========================= createOrgSubscription =========================

    @Test
    @DisplayName("Should create org subscription successfully")
    public void shouldCreateOrgSubscriptionSuccessfully() {
        // Given: an active organization and an existing watchlist
        final Organization org = anOrganization(5L, OrganizationStatus.ACTIVE);
        final Watchlist watchlist = aWatchlistWithOrg(10L, user, org);
        when(organizationRepository.findById(5L)).thenReturn(Optional.of(org));
        when(watchlistRepository.findById(10L)).thenReturn(Optional.of(watchlist));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> {
            final Subscription sub = inv.getArgument(0);
            sub.setId(2L);
            return sub;
        });

        // When: creating org subscription
        final CreateSubscriptionRequest request = aValidCreateRequest(10L);
        final Subscription result = subscriptionService.createOrgSubscription(5L, request);

        // Then: subscription is created linked to the organization
        assertThat(result, is(notNullValue()));
        assertThat(result.getOrganization(), is(org));
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should throw exception when creating org subscription for suspended org")
    public void shouldThrowWhenCreatingOrgSubscriptionForSuspendedOrg() {
        // Given: a suspended organization
        final Organization suspendedOrg = anOrganization(5L, OrganizationStatus.SUSPENDED);
        when(organizationRepository.findById(5L)).thenReturn(Optional.of(suspendedOrg));

        // When / Then: forbidden exception is thrown
        assertThrows(
            RuntimeException.class,
            () -> subscriptionService.createOrgSubscription(5L, aValidCreateRequest(10L))
        );
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when org does not exist on create org subscription")
    public void shouldThrowWhenOrgNotFoundOnCreateOrgSubscription() {
        // Given: organization does not exist
        when(organizationRepository.findById(99L)).thenReturn(Optional.empty());

        // When / Then: DataNotFoundException is thrown
        assertThrows(
            DataNotFoundException.class,
            () -> subscriptionService.createOrgSubscription(99L, aValidCreateRequest(10L))
        );
    }

    // ========================= updateOrgSubscription =========================

    @Test
    @DisplayName("Should update org subscription successfully")
    public void shouldUpdateOrgSubscriptionSuccessfully() {
        // Given: an existing org subscription belonging to the org
        final Organization org = anOrganization(5L, OrganizationStatus.ACTIVE);
        final Watchlist watchlist = aWatchlistWithOrg(10L, user, org);
        final Subscription existing = aSubscription(2L, watchlist, user, org);
        when(subscriptionRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        // When: updating the org subscription
        final UpdateSubscriptionRequest request = aValidUpdateRequest();
        final Subscription result = subscriptionService.updateOrgSubscription(5L, 2L, request);

        // Then: subscription is updated
        assertThat(result.getId(), is(2L));
        verify(subscriptionRepository).save(existing);
    }

    @Test
    @DisplayName("Should throw exception when updating subscription belonging to different org")
    public void shouldThrowWhenUpdatingSubscriptionBelongingToDifferentOrg() {
        // Given: subscription belongs to a different org
        final Organization orgA = anOrganization(5L, OrganizationStatus.ACTIVE);
        final Organization orgB = anOrganization(6L, OrganizationStatus.ACTIVE);
        final Watchlist watchlist = aWatchlistWithOrg(10L, user, orgA);
        final Subscription existing = aSubscription(2L, watchlist, user, orgA);
        when(subscriptionRepository.findById(2L)).thenReturn(Optional.of(existing));

        // When / Then: forbidden/not-found exception is thrown
        assertThrows(
            RuntimeException.class,
            () -> subscriptionService.updateOrgSubscription(6L, 2L, aValidUpdateRequest())
        );
        verify(subscriptionRepository, never()).save(any());
    }

    // ========================= deleteOrgSubscription =========================

    @Test
    @DisplayName("Should delete org subscription successfully")
    public void shouldDeleteOrgSubscriptionSuccessfully() {
        // Given: an existing org subscription belonging to the org
        final Organization org = anOrganization(5L, OrganizationStatus.ACTIVE);
        final Watchlist watchlist = aWatchlistWithOrg(10L, user, org);
        final Subscription existing = aSubscription(2L, watchlist, user, org);
        when(subscriptionRepository.findById(2L)).thenReturn(Optional.of(existing));

        // When: deleting the org subscription
        subscriptionService.deleteOrgSubscription(5L, 2L);

        // Then: subscription is deleted
        verify(subscriptionRepository).delete(existing);
    }

    @Test
    @DisplayName("Should throw exception when deleting subscription belonging to different org")
    public void shouldThrowWhenDeletingSubscriptionBelongingToDifferentOrg() {
        // Given: subscription belongs to a different org
        final Organization orgA = anOrganization(5L, OrganizationStatus.ACTIVE);
        final Watchlist watchlist = aWatchlistWithOrg(10L, user, orgA);
        final Subscription existing = aSubscription(2L, watchlist, user, orgA);
        when(subscriptionRepository.findById(2L)).thenReturn(Optional.of(existing));

        // When / Then: forbidden/not-found exception is thrown
        assertThrows(
            RuntimeException.class,
            () -> subscriptionService.deleteOrgSubscription(6L, 2L)
        );
        verify(subscriptionRepository, never()).delete(any(Subscription.class));
    }

    // ========================= searchOrgSubscriptions =========================

    @Test
    @DisplayName("Should return paginated org subscriptions filtered to org")
    public void shouldReturnPaginatedOrgSubscriptions() {
        // Given: org subscriptions exist
        final Organization org = anOrganization(5L, OrganizationStatus.ACTIVE);
        final Watchlist watchlist = aWatchlistWithOrg(10L, user, org);
        final Subscription sub = aSubscription(2L, watchlist, user, org);
        final Page<Subscription> page = new PageImpl<>(List.of(sub));
        when(subscriptionRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        // When: searching org subscriptions
        final Page<Subscription> result = subscriptionService.searchOrgSubscriptions(5L, new SortablePageInput());

        // Then: page with org subscriptions is returned
        assertThat(result.getTotalElements(), is(1L));
        assertThat(result.getContent(), hasSize(1));
    }

    // ========================= FACTORY METHODS =========================

    private static CreateSubscriptionRequest aValidCreateRequest(final Long watchlistId) {
        final CreateSubscriptionRequest request = new CreateSubscriptionRequest();
        request.setWatchlistId(watchlistId);
        request.setTargetSeverities(List.of(Severity.CRITICAL));
        request.setAdapterConfigIds(List.of(1L));
        return request;
    }

    private static UpdateSubscriptionRequest aValidUpdateRequest() {
        final UpdateSubscriptionRequest request = new UpdateSubscriptionRequest();
        request.setTargetSeverities(List.of(Severity.WARNING));
        request.setAdapterConfigIds(List.of(1L));
        return request;
    }

    private static Organization anOrganization(final Long id, final OrganizationStatus status) {
        final Organization org = new Organization("Test Org", "test-org");
        org.setId(id);
        org.setStatus(status);
        return org;
    }

    private static Watchlist aWatchlistWithOrg(final Long id, final User user, final Organization org) {
        final Watchlist watchlist = new Watchlist("Test Watchlist", user, org);
        watchlist.setId(id);
        return watchlist;
    }

    private static Subscription aSubscription(final Long id, final Watchlist watchlist, final User user,
        final Organization org) {
        final Subscription subscription = new Subscription();
        subscription.setId(id);
        subscription.setUser(user);
        subscription.setWatchlist(watchlist);
        subscription.setOrganization(org);
        subscription.setTargetSeverities(List.of(Severity.CRITICAL));
        subscription.setAdapterConfigIds(List.of(1L));
        subscription.setCreatedAt(OffsetDateTime.now().minusDays(1));
        return subscription;
    }
}
