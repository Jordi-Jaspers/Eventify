package io.github.eventify.api.event.repository;

import io.github.eventify.api.event.model.Event;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Event entity.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

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
     * Deletes expired events from personal channels based on user retention settings. Uses a subquery with LIMIT to batch deletions and
     * avoid long-running transactions.
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
     * Deletes expired events from organization channels based on org retention settings. Uses a subquery with LIMIT to batch deletions and
     * avoid long-running transactions.
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
     * Finds all events for multiple channels within a time range, plus the last event before the range for each channel (to establish
     * initial severity state). Uses a CTE to efficiently combine both queries in a single database round-trip.
     *
     * @param channelIds the channel IDs
     * @param startTime  start of time range
     * @param endTime    end of time range
     * @return list of events ordered by timestamp (oldest first), including prior events
     */
    @Query(
        value = """
            WITH last_before_range AS (
                SELECT DISTINCT ON (channel_id) channel_id, timestamp AS max_time
                FROM event
                WHERE timestamp < :startTime
                  AND channel_id IN (:channelIds)
                ORDER BY channel_id, timestamp DESC
            )
            SELECT e.*
            FROM (
                SELECT ev.*
                FROM event ev
                INNER JOIN last_before_range lb
                    ON lb.channel_id = ev.channel_id
                   AND lb.max_time = ev.timestamp
                UNION ALL
                SELECT ev.*
                FROM event ev
                WHERE ev.channel_id IN (:channelIds)
                  AND ev.timestamp BETWEEN :startTime AND :endTime
            ) AS e
            ORDER BY e.channel_id ASC, e.timestamp ASC
            """,
        nativeQuery = true
    )
    List<Event> findEventsWithLastBeforeRange(
        @Param("channelIds") List<Long> channelIds,
        @Param("startTime") OffsetDateTime startTime,
        @Param("endTime") OffsetDateTime endTime
    );
}
