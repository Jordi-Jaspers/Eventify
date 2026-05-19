package io.github.eventify.api.admin.repository;

import io.github.eventify.api.admin.model.projection.DailyEventIngestion;
import io.github.eventify.api.admin.model.projection.TopChannelData;
import io.github.eventify.api.event.model.Event;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for querying the event_timeline_hourly TimescaleDB continuous aggregate.
 */
@Repository
public interface EventTimelineRepository extends JpaRepository<Event, Long> {

    @Query(
        nativeQuery = true,
        value = """
            SELECT DATE(bucket) AS date, SUM(event_count) AS event_count
            FROM event_timeline_hourly
            WHERE bucket >= :from AND bucket < :to
            GROUP BY DATE(bucket)
            ORDER BY DATE(bucket) ASC
            """
    )
    List<DailyEventIngestion> findDailyIngestion(
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to
    );

    @Query(
        nativeQuery = true,
        value = """
            SELECT
                eth.channel_id AS channel_id,
                c.name AS channel_name,
                COALESCE(o.name, u.email) AS owner_name,
                SUM(eth.event_count) AS event_count,
                CASE WHEN SUM(SUM(eth.event_count)) OVER () = 0 THEN 0.0
                     ELSE SUM(eth.event_count) * 100.0 / SUM(SUM(eth.event_count)) OVER ()
                END AS percentage
            FROM event_timeline_hourly eth
            JOIN channel c ON c.id = eth.channel_id
            LEFT JOIN organization o ON o.id = c.organization_id
            LEFT JOIN "user" u ON u.id = c.user_id
            WHERE eth.bucket >= :from AND eth.bucket < :to
            GROUP BY eth.channel_id, c.name, o.name, u.email
            ORDER BY SUM(eth.event_count) DESC
            LIMIT :limit
            """
    )
    List<TopChannelData> findTopChannels(
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to,
        @Param("limit") int limit
    );

    @Query(
        nativeQuery = true,
        value = """
            SELECT COALESCE(SUM(event_count), 0)
            FROM event_timeline_hourly
            WHERE bucket >= :from AND bucket < :to AND last_severity = :severity
            """
    )
    Long countBySeverity(
        @Param("from") OffsetDateTime from,
        @Param("to") OffsetDateTime to,
        @Param("severity") String severity
    );
}
