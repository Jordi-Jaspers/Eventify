package io.github.eventify.api.watchlist.repository;

import io.github.eventify.api.watchlist.model.WatchlistChannel;
import io.github.eventify.api.watchlist.model.WatchlistChannelId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for WatchlistChannel entity.
 */
@Repository
public interface WatchlistChannelRepository extends JpaRepository<WatchlistChannel, WatchlistChannelId> {

    /**
     * Finds all watchlist channels by watchlist ID.
     *
     * @param watchlistId the watchlist ID
     * @return list of watchlist channels
     */
    @Query(
        """
            SELECT wc FROM WatchlistChannel wc
            JOIN FETCH wc.channel
            WHERE wc.watchlist.id = :watchlistId
            ORDER BY wc.position
            """
    )
    List<WatchlistChannel> findByWatchlistId(@Param("watchlistId") Long watchlistId);

    /**
     * Deletes all watchlist channels by watchlist ID.
     *
     * @param watchlistId the watchlist ID
     */
    @Modifying
    @Query("DELETE FROM WatchlistChannel wc WHERE wc.watchlist.id = :watchlistId")
    void deleteAllByWatchlistId(@Param("watchlistId") Long watchlistId);
}
