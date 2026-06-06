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
import io.github.eventify.common.exception.ApiErrorCode;
import io.github.eventify.common.exception.OrganizationSuspendedException;
import io.github.eventify.common.security.SecurityUtil;
import io.github.eventify.common.util.TimeProvider;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing watchlist subscriptions.
 */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final SubscriptionRepository subscriptionRepository;
    private final WatchlistRepository watchlistRepository;
    private final OrganizationRepository organizationRepository;
    private final SubscriptionMetaData subscriptionMetaData;

    /**
     * Creates or upserts a personal subscription for the current user.
     *
     * @param request the create request
     * @return the saved subscription
     */
    @Transactional
    public Subscription createPersonalSubscription(final CreateSubscriptionRequest request) {
        final User user = SecurityUtil.getLoggedInUser();
        final Watchlist watchlist = findWatchlistOrThrow(request.getWatchlistId());

        verifyOrgNotSuspended(watchlist.getOrganization());

        final Subscription subscription = subscriptionRepository
            .findByWatchlistIdAndUserIdAndOrganizationIsNull(watchlist.getId(), user.getId())
            .orElseGet(() -> {
                final Subscription newSub = new Subscription();
                newSub.setWatchlist(watchlist);
                newSub.setUser(user);
                return newSub;
            });

        applyFields(subscription, request.getTargetSeverities(), request.getAdapterConfigIds());

        return subscriptionRepository.save(subscription);
    }

    /**
     * Updates a personal subscription by ID. Verifies ownership.
     *
     * @param id      the subscription ID
     * @param request the update request
     * @return the updated subscription
     */
    @Transactional
    public Subscription updatePersonalSubscription(final Long id, final UpdateSubscriptionRequest request) {
        final User user = SecurityUtil.getLoggedInUser();
        final Subscription subscription = findSubscriptionOrThrow(id);

        verifyUserOwnership(subscription, user);
        verifyOrgNotSuspended(subscription.getWatchlist().getOrganization());

        applyFields(subscription, request.getTargetSeverities(), request.getAdapterConfigIds());

        return subscriptionRepository.save(subscription);
    }

    /**
     * Deletes a personal subscription by ID. Verifies ownership.
     *
     * @param id the subscription ID
     */
    @Transactional
    public void deletePersonalSubscription(final Long id) {
        final User user = SecurityUtil.getLoggedInUser();
        final Subscription subscription = findSubscriptionOrThrow(id);
        verifyUserOwnership(subscription, user);
        subscriptionRepository.delete(subscription);
    }

    /**
     * Searches personal subscriptions for the current user.
     *
     * @param input the page input
     * @return paginated results
     */
    @Transactional(readOnly = true)
    public Page<Subscription> searchPersonalSubscriptions(final SortablePageInput input) {
        final User user = SecurityUtil.getLoggedInUser();
        final Specification<Subscription> spec = subscriptionMetaData.toPersonalSpecification(input, user.getId());
        return subscriptionRepository.findAll(spec, buildPageable(input));
    }

    /**
     * Creates an org subscription.
     *
     * @param orgId   the organization ID
     * @param request the create request
     * @return the saved subscription
     */
    @Transactional
    public Subscription createOrgSubscription(final Long orgId, final CreateSubscriptionRequest request) {
        final Organization org = findOrganizationOrThrow(orgId);
        verifyOrgNotSuspended(org);

        final Watchlist watchlist = findWatchlistOrThrow(request.getWatchlistId());
        final User user = SecurityUtil.getLoggedInUser();

        final Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setWatchlist(watchlist);
        subscription.setOrganization(org);
        applyFields(subscription, request.getTargetSeverities(), request.getAdapterConfigIds());

        return subscriptionRepository.save(subscription);
    }

    /**
     * Updates an org subscription.
     *
     * @param orgId   the organization ID
     * @param id      the subscription ID
     * @param request the update request
     * @return the updated subscription
     */
    @Transactional
    public Subscription updateOrgSubscription(final Long orgId, final Long id, final UpdateSubscriptionRequest request) {
        final Subscription subscription = findSubscriptionOrThrow(id);
        verifyOrgOwnership(subscription, orgId);
        verifyOrgNotSuspended(subscription.getOrganization());

        applyFields(subscription, request.getTargetSeverities(), request.getAdapterConfigIds());

        return subscriptionRepository.save(subscription);
    }

    /**
     * Deletes an org subscription.
     *
     * @param orgId the organization ID
     * @param id    the subscription ID
     */
    @Transactional
    public void deleteOrgSubscription(final Long orgId, final Long id) {
        final Subscription subscription = findSubscriptionOrThrow(id);
        verifyOrgOwnership(subscription, orgId);
        subscriptionRepository.delete(subscription);
    }

    /**
     * Searches org subscriptions filtered to the given org.
     *
     * @param orgId the organization ID
     * @param input the page input
     * @return paginated results
     */
    @Transactional(readOnly = true)
    public Page<Subscription> searchOrgSubscriptions(final Long orgId, final SortablePageInput input) {
        final Specification<Subscription> spec = subscriptionMetaData.toOrgSpecification(input, orgId);
        return subscriptionRepository.findAll(spec, buildPageable(input));
    }

    private Pageable buildPageable(final SortablePageInput input) {
        final Sort sort = subscriptionMetaData.toSort(input.getSortOrder());
        final int pageSize = input.getPageSize() > 0 ? input.getPageSize() : DEFAULT_PAGE_SIZE;
        return PageRequest.of(input.getPageNumber(), pageSize, sort);
    }

    private Watchlist findWatchlistOrThrow(final Long watchlistId) {
        return watchlistRepository.findById(watchlistId)
            .orElseThrow(() -> new DataNotFoundException(ApiErrorCode.WATCHLIST_NOT_FOUND));
    }

    private Organization findOrganizationOrThrow(final Long orgId) {
        return organizationRepository.findById(orgId)
            .orElseThrow(() -> new DataNotFoundException(ApiErrorCode.ORGANIZATION_NOT_FOUND_ERROR));
    }

    private Subscription findSubscriptionOrThrow(final Long id) {
        return subscriptionRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException(ApiErrorCode.SUBSCRIPTION_NOT_FOUND));
    }

    private void verifyUserOwnership(final Subscription subscription, final User user) {
        if (!subscription.getUser().getId().equals(user.getId())) {
            throw new DataNotFoundException(ApiErrorCode.SUBSCRIPTION_NOT_FOUND);
        }
    }

    private void verifyOrgOwnership(final Subscription subscription, final Long orgId) {
        final Organization org = subscription.getOrganization();
        if (org == null || !org.getId().equals(orgId)) {
            throw new DataNotFoundException(ApiErrorCode.SUBSCRIPTION_NOT_FOUND);
        }
    }

    private void verifyOrgNotSuspended(final Organization organization) {
        if (organization != null && OrganizationStatus.SUSPENDED.equals(organization.getStatus())) {
            throw new OrganizationSuspendedException();
        }
    }

    private void applyFields(final Subscription subscription, final List<Severity> targetSeverities, final List<Long> adapterConfigIds) {
        subscription.setTargetSeverities(targetSeverities);
        subscription.setAdapterConfigIds(adapterConfigIds);
        subscription.setUpdatedAt(TimeProvider.now());
    }
}
