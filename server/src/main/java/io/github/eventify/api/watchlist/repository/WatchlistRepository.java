package io.github.eventify.api.watchlist.repository;

import io.github.eventify.api.watchlist.model.Watchlist;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Watchlist entity.
 */
@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long>, JpaSpecificationExecutor<Watchlist> {

    /**
     * Finds a watchlist by ID and user ID.
     *
     * @param id     the watchlist ID
     * @param userId the user ID
     * @return optional watchlist
     */
    Optional<Watchlist> findByIdAndUserId(Long id, Long userId);

    /**
     * Finds a watchlist by user ID and name (case-insensitive).
     *
     * @param userId the user ID
     * @param name   the watchlist name
     * @return optional watchlist
     */
    @Query(
        """
            SELECT w FROM Watchlist w
            WHERE w.user.id = :userId
            AND LOWER(w.name) = LOWER(:name)
            AND w.organization IS NULL
            """
    )
    Optional<Watchlist> findByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    /**
     * Removes a channel ID from all watchlist configurations.
     *
     * @param channelId the channel ID to remove
     */
    @Modifying
    @Query(
        value = """
            UPDATE watchlist
            SET configuration = jsonb_set(
                configuration,
                '{channelIds}',
                (SELECT COALESCE(jsonb_agg(elem), '[]'::jsonb)
                 FROM jsonb_array_elements(configuration->'channelIds') elem
                 WHERE elem::text::bigint != :channelId)
            ),
            updated_at = NOW()
            WHERE configuration->'channelIds' @> to_jsonb(:channelId)
            """,
        nativeQuery = true
    )
    void removeChannelFromAllConfigurations(@Param("channelId") Long channelId);
}
