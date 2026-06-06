package io.github.eventify.api.subscription.repository;

import io.github.eventify.api.subscription.model.Subscription;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Subscription entity.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, JpaSpecificationExecutor<Subscription> {

    @Override
    @EntityGraph(
        attributePaths = {
            "watchlist",
            "watchlist.organization",
            "organization"
        }
    )
    Optional<Subscription> findById(Long id);

    @Override
    @EntityGraph(
        attributePaths = {
            "watchlist",
            "watchlist.organization",
            "organization"
        }
    )
    Page<Subscription> findAll(Specification<Subscription> spec, Pageable pageable);

    /**
     * Finds a personal subscription (no org) by watchlist ID and user ID.
     *
     * @param watchlistId the watchlist ID
     * @param userId      the user ID
     * @return optional subscription
     */
    Optional<Subscription> findByWatchlistIdAndUserIdAndOrganizationIsNull(Long watchlistId, Long userId);

    /**
     * Finds subscriptions by watchlist ID where targetSeverities contains the given severity.
     *
     * @param watchlistId the watchlist ID
     * @param severity    the severity string to match
     * @return list of matching subscriptions
     */
    @Query(
        value = """
            SELECT s.* FROM subscription s
            WHERE s.watchlist_id = :watchlistId
            AND s.target_severities @> to_jsonb(:severity::text)
            """,
        nativeQuery = true
    )
    List<Subscription> findByWatchlistIdAndTargetSeveritiesContaining(
        @Param("watchlistId") Long watchlistId,
        @Param("severity") String severity
    );

    /**
     * Finds subscriptions by watchlist IDs.
     *
     * @param watchlistIds the watchlist IDs
     * @return list of subscriptions
     */
    List<Subscription> findByWatchlistIdIn(List<Long> watchlistIds);
}
