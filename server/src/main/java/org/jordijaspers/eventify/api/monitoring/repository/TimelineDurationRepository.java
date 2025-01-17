package org.jordijaspers.eventify.api.monitoring.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.jordijaspers.eventify.api.monitoring.model.TimelineDuration;
import org.jordijaspers.eventify.api.monitoring.model.TimelineDurationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TimelineDurationRepository extends JpaRepository<TimelineDuration, TimelineDurationId> {

    @Query(
        value = """
            WITH aggregate AS (
                 SELECT check_id, state_agg(timestamp, status) agg
                 FROM event
                 WHERE timestamp > :startTime
                 AND check_id IN (:checkIds)
                 GROUP BY check_id
             )
             SELECT
                 check_id,
                 status,
                 start_time,
                 CASE\s
                     WHEN LEAD(start_time) OVER (PARTITION BY check_id ORDER BY start_time) IS NULL THEN NULL
                     ELSE COALESCE(end_time, LEAD(start_time) OVER (PARTITION BY check_id ORDER BY start_time), NOW())
                 END as end_time
             FROM (
                 SELECT
                     check_id,
                     (t.line).state      AS status,
                     (t.line).start_time AS start_time,
                     CASE
                      WHEN ((t.line).end_time > (t.line).start_time) THEN (t.line).end_time
                      ELSE NOW()
                     END                 AS end_time
                 FROM (SELECT aggregate.check_id, state_timeline(aggregate.agg) line FROM aggregate) t
                 UNION ALL
                 SELECT check_id, last(status, timestamp), :startTime as start_time, null
                 FROM event
                 WHERE timestamp < :startTime
                 AND check_id IN (:checkIds)
                 GROUP BY check_id
             ) result
             ORDER BY check_id, start_time
            """,
        nativeQuery = true
    )
    List<TimelineDuration> findDurationsForChecks(
        @Param("checkIds") Set<Long> checkIds,
        @Param("startTime") LocalDateTime startTime
    );
}
