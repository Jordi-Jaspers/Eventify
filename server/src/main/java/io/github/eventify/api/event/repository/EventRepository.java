package io.github.eventify.api.event.repository;

import io.github.eventify.api.event.model.Event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for Event entity.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Finds all events for a given channel, ordered by timestamp descending.
     *
     * @param channelId the channel ID
     * @return list of events ordered by timestamp (newest first)
     */
    List<Event> findByChannelIdOrderByTimestampDesc(Long channelId);

    /**
     * Counts all events for a given channel.
     *
     * @param channelId the channel ID
     * @return count of events
     */
    long countByChannelId(Long channelId);

    /**
     * Counts events across multiple channels.
     *
     * @param channelIds the channel IDs
     * @return count of events
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.channel.id IN :channelIds")
    long countByChannelIdIn(@Param("channelIds") List<Long> channelIds);

    /**
     * Deletes expired events from personal channels based on user retention settings.
     * Uses a subquery with LIMIT to batch deletions and avoid long-running transactions.
     *
     * @param batchSize maximum number of events to delete per call
     * @return number of events deleted
     */
    @Modifying
    @Query(
        value = """
            DELETE FROM event WHERE id IN (
                SELECT e.id FROM event e
                JOIN channel c ON e.channel_id = c.id
                JOIN "user" u ON c.user_id = u.id
                WHERE c.organization_id IS NULL
                  AND e.timestamp < NOW() - (u.retention_days || ' days')::INTERVAL
                LIMIT :batchSize
            )
            """,
        nativeQuery = true
    )
    int deleteExpiredPersonalChannelEvents(int batchSize);

    /**
     * Deletes expired events from organization channels based on org retention settings.
     * Uses a subquery with LIMIT to batch deletions and avoid long-running transactions.
     *
     * @param batchSize maximum number of events to delete per call
     * @return number of events deleted
     */
    @Modifying
    @Query(
        value = """
            DELETE FROM event WHERE id IN (
                SELECT e.id FROM event e
                JOIN channel c ON e.channel_id = c.id
                JOIN organization o ON c.organization_id = o.id
                WHERE e.timestamp < NOW() - (o.retention_days || ' days')::INTERVAL
                LIMIT :batchSize
            )
            """,
        nativeQuery = true
    )
    int deleteExpiredOrganizationChannelEvents(int batchSize);

    /**
     * Delete all events in channels owned by users with the given IDs.
     *
     * @param userIds the user IDs
     */
    @Modifying(
        clearAutomatically = true,
        flushAutomatically = true
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query("DELETE FROM Event e WHERE e.channel.user.id IN :userIds")
    void deleteAllByChannelUserIdIn(@Param("userIds") List<Long> userIds);
}
