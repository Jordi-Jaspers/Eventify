package io.github.eventify.api.monitor.repository;

import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.monitor.model.BucketSize;
import io.github.eventify.api.monitor.model.TimelineBucket;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for querying pre-aggregated timeline data from TimescaleDB continuous aggregates.
 * Uses Spring Data JPA with native queries for each fixed bucket size.
 */
@Repository
public interface TimelineAggregateRepository extends org.springframework.data.repository.Repository<Event, Long> {

    // ========================= findBucketsForChannels =========================

    /**
     * Finds aggregated 30-minute buckets for the given channels within the specified time range.
     *
     * @param channelIds the channel IDs to query
     * @param start      the start of the time range (inclusive)
     * @param end        the end of the time range (exclusive)
     * @return list of aggregated timeline buckets
     */
    @Query(
        value = """
            SELECT channel_id AS channelId,
                   time_bucket('1800 seconds'::INTERVAL, bucket) AS bucket,
                   FIRST(first_severity, bucket) AS firstSeverity,
                   LAST(last_severity, bucket) AS lastSeverity,
                   SUM(event_count) AS eventCount,
                   MIN(first_event_time) AS firstEventTime,
                   MAX(last_event_time) AS lastEventTime
            FROM event_timeline_hourly
            WHERE channel_id IN (:channelIds)
              AND bucket >= :start
              AND bucket < :end
            GROUP BY channel_id, time_bucket('1800 seconds'::INTERVAL, bucket)
            ORDER BY channel_id, bucket
            """,
        nativeQuery = true
    )
    List<TimelineBucket> findBucketsForChannels30m(
        @Param("channelIds") List<Long> channelIds,
        @Param("start") OffsetDateTime start,
        @Param("end") OffsetDateTime end
    );

    /**
     * Finds aggregated 2-hour buckets for the given channels within the specified time range.
     *
     * @param channelIds the channel IDs to query
     * @param start      the start of the time range (inclusive)
     * @param end        the end of the time range (exclusive)
     * @return list of aggregated timeline buckets
     */
    @Query(
        value = """
            SELECT channel_id AS channelId,
                   time_bucket('7200 seconds'::INTERVAL, bucket) AS bucket,
                   FIRST(first_severity, bucket) AS firstSeverity,
                   LAST(last_severity, bucket) AS lastSeverity,
                   SUM(event_count) AS eventCount,
                   MIN(first_event_time) AS firstEventTime,
                   MAX(last_event_time) AS lastEventTime
            FROM event_timeline_hourly
            WHERE channel_id IN (:channelIds)
              AND bucket >= :start
              AND bucket < :end
            GROUP BY channel_id, time_bucket('7200 seconds'::INTERVAL, bucket)
            ORDER BY channel_id, bucket
            """,
        nativeQuery = true
    )
    List<TimelineBucket> findBucketsForChannels2h(
        @Param("channelIds") List<Long> channelIds,
        @Param("start") OffsetDateTime start,
        @Param("end") OffsetDateTime end
    );

    /**
     * Finds aggregated 4-hour buckets for the given channels within the specified time range.
     *
     * @param channelIds the channel IDs to query
     * @param start      the start of the time range (inclusive)
     * @param end        the end of the time range (exclusive)
     * @return list of aggregated timeline buckets
     */
    @Query(
        value = """
            SELECT channel_id AS channelId,
                   time_bucket('14400 seconds'::INTERVAL, bucket) AS bucket,
                   FIRST(first_severity, bucket) AS firstSeverity,
                   LAST(last_severity, bucket) AS lastSeverity,
                   SUM(event_count) AS eventCount,
                   MIN(first_event_time) AS firstEventTime,
                   MAX(last_event_time) AS lastEventTime
            FROM event_timeline_hourly
            WHERE channel_id IN (:channelIds)
              AND bucket >= :start
              AND bucket < :end
            GROUP BY channel_id, time_bucket('14400 seconds'::INTERVAL, bucket)
            ORDER BY channel_id, bucket
            """,
        nativeQuery = true
    )
    List<TimelineBucket> findBucketsForChannels4h(
        @Param("channelIds") List<Long> channelIds,
        @Param("start") OffsetDateTime start,
        @Param("end") OffsetDateTime end
    );

    /**
     * Dispatches to the correct bucket-size-specific query.
     *
     * @param channelIds the channel IDs to query
     * @param start      the start of the time range (inclusive)
     * @param end        the end of the time range (exclusive)
     * @param bucketSize the target bucket size
     * @return list of aggregated timeline buckets
     */
    default List<TimelineBucket> findBucketsForChannels(
        final List<Long> channelIds,
        final OffsetDateTime start,
        final OffsetDateTime end,
        final BucketSize bucketSize
    ) {
        return switch (bucketSize) {
            case PT30M -> findBucketsForChannels30m(channelIds, start, end);
            case PT2H -> findBucketsForChannels2h(channelIds, start, end);
            case PT4H -> findBucketsForChannels4h(channelIds, start, end);
        };
    }

    // ========================= findPriorBuckets =========================

    /**
     * Finds the last 30-minute bucket before the given time for each channel.
     *
     * @param channelIds the channel IDs to query
     * @param beforeTime the exclusive upper boundary (prior to this time)
     * @return one prior bucket per channel (DISTINCT ON channel_id)
     */
    @Query(
        value = """
            SELECT DISTINCT ON (sub.channel_id)
                   sub.channel_id AS channelId,
                   sub.bucket,
                   sub.firstSeverity,
                   sub.lastSeverity,
                   sub.eventCount,
                   sub.firstEventTime,
                   sub.lastEventTime
            FROM (
                SELECT channel_id,
                       time_bucket('1800 seconds'::INTERVAL, bucket) AS bucket,
                       FIRST(first_severity, bucket) AS firstSeverity,
                       LAST(last_severity, bucket) AS lastSeverity,
                       SUM(event_count) AS eventCount,
                       MIN(first_event_time) AS firstEventTime,
                       MAX(last_event_time) AS lastEventTime
                FROM event_timeline_hourly
                WHERE channel_id IN (:channelIds)
                  AND bucket < :beforeTime
                GROUP BY channel_id, time_bucket('1800 seconds'::INTERVAL, bucket)
                ORDER BY channel_id, bucket DESC
            ) sub
            """,
        nativeQuery = true
    )
    List<TimelineBucket> findPriorBuckets30m(
        @Param("channelIds") List<Long> channelIds,
        @Param("beforeTime") OffsetDateTime beforeTime
    );

    /**
     * Finds the last 2-hour bucket before the given time for each channel.
     *
     * @param channelIds the channel IDs to query
     * @param beforeTime the exclusive upper boundary (prior to this time)
     * @return one prior bucket per channel (DISTINCT ON channel_id)
     */
    @Query(
        value = """
            SELECT DISTINCT ON (sub.channel_id)
                   sub.channel_id AS channelId,
                   sub.bucket,
                   sub.firstSeverity,
                   sub.lastSeverity,
                   sub.eventCount,
                   sub.firstEventTime,
                   sub.lastEventTime
            FROM (
                SELECT channel_id,
                       time_bucket('7200 seconds'::INTERVAL, bucket) AS bucket,
                       FIRST(first_severity, bucket) AS firstSeverity,
                       LAST(last_severity, bucket) AS lastSeverity,
                       SUM(event_count) AS eventCount,
                       MIN(first_event_time) AS firstEventTime,
                       MAX(last_event_time) AS lastEventTime
                FROM event_timeline_hourly
                WHERE channel_id IN (:channelIds)
                  AND bucket < :beforeTime
                GROUP BY channel_id, time_bucket('7200 seconds'::INTERVAL, bucket)
                ORDER BY channel_id, bucket DESC
            ) sub
            """,
        nativeQuery = true
    )
    List<TimelineBucket> findPriorBuckets2h(
        @Param("channelIds") List<Long> channelIds,
        @Param("beforeTime") OffsetDateTime beforeTime
    );

    /**
     * Finds the last 4-hour bucket before the given time for each channel.
     *
     * @param channelIds the channel IDs to query
     * @param beforeTime the exclusive upper boundary (prior to this time)
     * @return one prior bucket per channel (DISTINCT ON channel_id)
     */
    @Query(
        value = """
            SELECT DISTINCT ON (sub.channel_id)
                   sub.channel_id AS channelId,
                   sub.bucket,
                   sub.firstSeverity,
                   sub.lastSeverity,
                   sub.eventCount,
                   sub.firstEventTime,
                   sub.lastEventTime
            FROM (
                SELECT channel_id,
                       time_bucket('14400 seconds'::INTERVAL, bucket) AS bucket,
                       FIRST(first_severity, bucket) AS firstSeverity,
                       LAST(last_severity, bucket) AS lastSeverity,
                       SUM(event_count) AS eventCount,
                       MIN(first_event_time) AS firstEventTime,
                       MAX(last_event_time) AS lastEventTime
                FROM event_timeline_hourly
                WHERE channel_id IN (:channelIds)
                  AND bucket < :beforeTime
                GROUP BY channel_id, time_bucket('14400 seconds'::INTERVAL, bucket)
                ORDER BY channel_id, bucket DESC
            ) sub
            """,
        nativeQuery = true
    )
    List<TimelineBucket> findPriorBuckets4h(
        @Param("channelIds") List<Long> channelIds,
        @Param("beforeTime") OffsetDateTime beforeTime
    );

    /**
     * Dispatches to the correct bucket-size-specific prior bucket query.
     *
     * @param channelIds the channel IDs to query
     * @param beforeTime the exclusive upper boundary (prior to this time)
     * @param bucketSize the target bucket size
     * @return one prior bucket per channel
     */
    default List<TimelineBucket> findPriorBuckets(
        final List<Long> channelIds,
        final OffsetDateTime beforeTime,
        final BucketSize bucketSize
    ) {
        return switch (bucketSize) {
            case PT30M -> findPriorBuckets30m(channelIds, beforeTime);
            case PT2H -> findPriorBuckets2h(channelIds, beforeTime);
            case PT4H -> findPriorBuckets4h(channelIds, beforeTime);
        };
    }
}
