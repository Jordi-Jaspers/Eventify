package io.github.eventify.api.admin.stats.repository;

import io.github.eventify.api.admin.stats.model.projection.DailyEventStats;
import io.github.eventify.api.event.model.Event;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for querying the admin_event_stats_daily continuous aggregate.
 */
@Repository
public interface AdminEventStatsDailyRepository extends JpaRepository<Event, Long> {

    /**
     * Returns daily event counts for a date range.
     */
    @Query(
        nativeQuery = true,
        value = """
            SELECT day::date AS day, total_events AS totalEvents
            FROM admin_event_stats_daily
            WHERE day >= :from AND day < :to
            ORDER BY day ASC
            """
    )
    List<DailyEventStats> findDailyStats(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    /**
     * Returns total event count for a date range.
     */
    @Query(
        nativeQuery = true,
        value = """
            SELECT COALESCE(SUM(total_events), 0)
            FROM admin_event_stats_daily
            WHERE day >= :from AND day < :to
            """
    )
    Long countTotalEvents(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}
