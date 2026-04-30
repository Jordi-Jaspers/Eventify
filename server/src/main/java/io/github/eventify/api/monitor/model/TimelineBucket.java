package io.github.eventify.api.monitor.model;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Interface-based projection for aggregate timeline buckets from TimescaleDB time_bucket queries.
 * Each bucket represents a time window summarizing events for a channel.
 *
 * <p>Spring Data proxies interface projections with automatic type conversion.
 * PostgreSQL JDBC returns {@link Instant} for timestamptz columns.
 */
public interface TimelineBucket {

    /** Returns the channel ID. */
    Long getChannelId();

    /** Returns the bucket timestamp as Instant. */
    Instant getBucket();

    /** Returns the first (earliest) severity in this bucket. */
    String getFirstSeverity();

    /** Returns the last (most recent) severity in this bucket. */
    String getLastSeverity();

    /** Returns the number of events in this bucket. */
    Long getEventCount();

    /** Returns the timestamp of the first event in this bucket. */
    Instant getFirstEventTime();

    /** Returns the timestamp of the last event in this bucket. */
    Instant getLastEventTime();

    /**
     * Returns the bucket time as OffsetDateTime (UTC).
     */
    default OffsetDateTime getBucketTime() {
        return getBucket().atOffset(ZoneOffset.UTC);
    }

    /**
     * Creates a concrete instance for testing or programmatic use.
     */
    static TimelineBucket of(
        final Long channelId,
        final OffsetDateTime bucket,
        final String firstSeverity,
        final String lastSeverity,
        final Long eventCount,
        final OffsetDateTime firstEventTime,
        final OffsetDateTime lastEventTime
    ) {
        return new TimelineBucketImpl(
            channelId,
            bucket.toInstant(),
            firstSeverity,
            lastSeverity,
            eventCount,
            firstEventTime.toInstant(),
            lastEventTime.toInstant()
        );
    }
}
