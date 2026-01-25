package io.github.eventify.api.watchlist.service;

import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * Security service for watchlist access control.
 * Bean name "watchlistSecurity" for use in SpEL expressions.
 */
@Service("watchlistSecurity")
@RequiredArgsConstructor
public class WatchlistSecurityService {

    private final WatchlistRepository watchlistRepository;
    private final OrganizationMembershipRepository membershipRepository;

    /**
     * Check if user owns this personal watchlist.
     *
     * @param watchlistId the watchlist ID
     * @param userId      the user ID
     * @return true if user owns the watchlist, false otherwise
     */
    public boolean canAccessUserWatchlist(final Long watchlistId, final Long userId) {
        if (watchlistId == null || userId == null) {
            return false;
        }

        final Watchlist watchlist = watchlistRepository.findById(watchlistId).orElse(null);
        return watchlist != null
            && watchlist.getUser() != null
            && watchlist.getUser().getId().equals(userId);
    }

    /**
     * Check if user can access organization watchlist.
     *
     * @param watchlistId the watchlist ID
     * @param orgId       the organization ID
     * @param userId      the user ID
     * @return true if user can access the watchlist, false otherwise
     */
    public boolean canAccessOrgWatchlist(final Long watchlistId, final Long orgId, final Long userId) {
        if (watchlistId == null || orgId == null || userId == null) {
            return false;
        }

        final Watchlist watchlist = watchlistRepository.findById(watchlistId).orElse(null);
        final boolean watchlistValid = watchlist != null
            && watchlist.getOrganization() != null
            && watchlist.getOrganization().getId().equals(orgId);

        return watchlistValid && membershipRepository.existsByOrganizationIdAndUserId(orgId, userId);
    }
}
