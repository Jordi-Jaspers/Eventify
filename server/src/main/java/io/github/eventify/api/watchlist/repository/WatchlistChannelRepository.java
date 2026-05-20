package io.github.eventify.api.watchlist.repository;

import io.github.eventify.api.watchlist.model.WatchlistChannel;
import io.github.eventify.api.watchlist.model.WatchlistChannelId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for WatchlistChannel junction entity.
 */
@Repository
public interface WatchlistChannelRepository extends JpaRepository<WatchlistChannel, WatchlistChannelId> {

    /**
     * Deletes all junction rows for the given watchlist.
     *
     * @param watchlistId the watchlist ID
     */
    @Modifying
    @Transactional
    void deleteByWatchlistId(Long watchlistId);

    /**
     * Finds all watchlist IDs that contain the given channel.
     *
     * @param channelId the channel ID
     * @return list of watchlist IDs
     */
    @Query("SELECT wc.watchlistId FROM WatchlistChannel wc WHERE wc.channelId = :channelId")
    List<Long> findWatchlistIdsByChannelId(@Param("channelId") Long channelId);

    /**
     * Bulk-inserts a junction row, ignoring conflicts.
     *
     * @param watchlistId the watchlist ID
     * @param channelId   the channel ID
     */
    @Modifying
    @Query(
        value = "INSERT INTO watchlist_channel (watchlist_id, channel_id) VALUES (:watchlistId, :channelId) ON CONFLICT DO NOTHING",
        nativeQuery = true
    )
    void insertIgnoreConflict(@Param("watchlistId") Long watchlistId, @Param("channelId") Long channelId);
}
