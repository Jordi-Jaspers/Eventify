package io.github.eventify.api.subscription.service;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.github.eventify.api.subscription.model.Subscription;
import io.github.eventify.api.subscription.model.request.SubscribeRequest;
import io.github.eventify.api.subscription.repository.SubscriptionRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.common.security.SecurityUtil;
import io.github.eventify.support.UnitTest;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Unit Test - Subscription Service")
public class SubscriptionServiceTest extends UnitTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

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

    // ========================= subscribe =========================

    @Test
    @DisplayName("Should create subscription when none exists")
    public void shouldCreateSubscriptionWhenNoneExists() {
        // Given: No existing subscription for this user and watchlist
        final Long watchlistId = 10L;
        final SubscribeRequest request = aValidSubscribeRequest();

        when(subscriptionRepository.findByWatchlistIdAndUserId(watchlistId, user.getId()))
            .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> {
            final Subscription sub = invocation.getArgument(0);
            sub.setId(1L);
            return sub;
        });

        // When: Subscribing
        final Subscription result = subscriptionService.subscribe(watchlistId, request);

        // Then: Subscription should be created
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(1L));
        assertThat(result.getTargetSeverities(), hasItems(Severity.CRITICAL));
        assertThat(result.getAdapters(), hasItems(AdapterType.IN_APP));

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should update existing subscription (upsert)")
    public void shouldUpdateExistingSubscriptionOnUpsert() {
        // Given: An existing subscription
        final Long watchlistId = 10L;
        final Subscription existing = aSubscription(1L, watchlistId, user);

        when(subscriptionRepository.findByWatchlistIdAndUserId(watchlistId, user.getId()))
            .thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // And: Updated request with different severities
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of(Severity.CRITICAL, Severity.WARNING));
        request.setAdapters(List.of(AdapterType.IN_APP));

        // When: Subscribing again
        final Subscription result = subscriptionService.subscribe(watchlistId, request);

        // Then: Existing subscription should be updated
        assertThat(result.getTargetSeverities(), hasItems(Severity.CRITICAL, Severity.WARNING));
        assertThat(result.getId(), is(1L));

        verify(subscriptionRepository).save(existing);
    }

    // ========================= getSubscription =========================

    @Test
    @DisplayName("Should return subscription when it exists")
    public void shouldReturnSubscriptionWhenItExists() {
        // Given: An existing subscription
        final Long watchlistId = 10L;
        final Subscription subscription = aSubscription(1L, watchlistId, user);

        when(subscriptionRepository.findByWatchlistIdAndUserId(watchlistId, user.getId()))
            .thenReturn(Optional.of(subscription));

        // When: Getting subscription
        final Subscription result = subscriptionService.getSubscription(watchlistId);

        // Then: Subscription should be returned
        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(1L));
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when subscription does not exist")
    public void shouldThrowDataNotFoundExceptionWhenSubscriptionDoesNotExist() {
        // Given: No subscription exists
        final Long watchlistId = 10L;

        when(subscriptionRepository.findByWatchlistIdAndUserId(watchlistId, user.getId()))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> subscriptionService.getSubscription(watchlistId)
        );
    }

    // ========================= unsubscribe =========================

    @Test
    @DisplayName("Should delete subscription when it exists")
    public void shouldDeleteSubscriptionWhenItExists() {
        // Given: An existing subscription
        final Long watchlistId = 10L;
        final Subscription subscription = aSubscription(1L, watchlistId, user);

        when(subscriptionRepository.findByWatchlistIdAndUserId(watchlistId, user.getId()))
            .thenReturn(Optional.of(subscription));

        // When: Unsubscribing
        subscriptionService.unsubscribe(watchlistId);

        // Then: Subscription should be deleted
        verify(subscriptionRepository).delete(subscription);
    }

    @Test
    @DisplayName("Should throw DataNotFoundException when unsubscribing without subscription")
    public void shouldThrowDataNotFoundExceptionWhenUnsubscribingWithoutSubscription() {
        // Given: No subscription exists
        final Long watchlistId = 10L;

        when(subscriptionRepository.findByWatchlistIdAndUserId(watchlistId, user.getId()))
            .thenReturn(Optional.empty());

        // When & Then: Should throw DataNotFoundException
        assertThrows(
            DataNotFoundException.class,
            () -> subscriptionService.unsubscribe(watchlistId)
        );

        verify(subscriptionRepository, never()).delete(any(Subscription.class));
    }

    // ========================= FACTORY METHODS =========================

    private static SubscribeRequest aValidSubscribeRequest() {
        final SubscribeRequest request = new SubscribeRequest();
        request.setTargetSeverities(List.of(Severity.CRITICAL));
        request.setAdapters(List.of(AdapterType.IN_APP));
        return request;
    }

    private static Subscription aSubscription(final Long id, final Long watchlistId, final User user) {
        final Watchlist watchlist = new Watchlist();
        watchlist.setId(watchlistId);

        final Subscription subscription = new Subscription();
        subscription.setId(id);
        subscription.setUser(user);
        subscription.setWatchlist(watchlist);
        subscription.setTargetSeverities(List.of(Severity.CRITICAL));
        subscription.setAdapters(List.of(AdapterType.IN_APP));
        subscription.setCreatedAt(OffsetDateTime.now().minusDays(1));
        return subscription;
    }
}
