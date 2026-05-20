package io.github.eventify.api.subscription.service;

import io.github.eventify.api.subscription.model.Subscription;
import io.github.eventify.api.subscription.model.request.SubscribeRequest;
import io.github.eventify.api.subscription.repository.SubscriptionRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.common.security.SecurityUtil;
import io.github.eventify.common.util.TimeProvider;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.exception.ApiErrorCode.WATCHLIST_NOT_FOUND;

/**
 * Service for managing watchlist subscriptions.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * Creates or updates a subscription for the current user on the given watchlist.
     *
     * @param watchlistId the watchlist ID
     * @param request     the subscribe request
     * @return the saved subscription
     */
    @Transactional
    public Subscription subscribe(final Long watchlistId, final SubscribeRequest request) {
        final User user = SecurityUtil.getLoggedInUser();

        final Subscription subscription = subscriptionRepository
            .findByWatchlistIdAndUserId(watchlistId, user.getId())
            .orElseGet(() -> {
                final Subscription newSub = new Subscription();
                final Watchlist watchlist = new Watchlist();
                watchlist.setId(watchlistId);
                newSub.setWatchlist(watchlist);
                newSub.setUser(user);
                return newSub;
            });

        subscription.setTargetSeverities(request.getTargetSeverities());
        subscription.setAdapters(request.getAdapters());
        subscription.setUpdatedAt(TimeProvider.now());

        return subscriptionRepository.save(subscription);
    }

    /**
     * Gets the subscription for the current user on the given watchlist.
     *
     * @param watchlistId the watchlist ID
     * @return the subscription
     * @throws DataNotFoundException if no subscription exists
     */
    @Transactional(readOnly = true)
    public Subscription getSubscription(final Long watchlistId) {
        final User user = SecurityUtil.getLoggedInUser();
        return findSubscriptionOrThrow(watchlistId, user.getId());
    }

    /**
     * Deletes the subscription for the current user on the given watchlist.
     *
     * @param watchlistId the watchlist ID
     * @throws DataNotFoundException if no subscription exists
     */
    @Transactional
    public void unsubscribe(final Long watchlistId) {
        final User user = SecurityUtil.getLoggedInUser();
        final Subscription subscription = findSubscriptionOrThrow(watchlistId, user.getId());
        subscriptionRepository.delete(subscription);
    }

    private Subscription findSubscriptionOrThrow(final Long watchlistId, final Long userId) {
        return subscriptionRepository
            .findByWatchlistIdAndUserId(watchlistId, userId)
            .orElseThrow(() -> new DataNotFoundException(WATCHLIST_NOT_FOUND));
    }
}
