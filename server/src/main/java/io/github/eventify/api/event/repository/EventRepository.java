package io.github.eventify.api.event.repository;

import io.github.eventify.api.event.model.Event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
}
