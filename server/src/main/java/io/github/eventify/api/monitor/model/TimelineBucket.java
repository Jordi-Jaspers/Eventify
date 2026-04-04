package io.github.eventify.api.monitor.model;

import java.time.OffsetDateTime;

/**
 * Projection interface for aggregate timeline buckets from TimescaleDB time_bucket queries.
 * Each bucket represents a time window summarizing events for a channel.
 */
public interface TimelineBucket {

    /**
     * Returns the channel ID this bucket belongs to.
     *
     * @return channel ID
     */
    Long getChannelId();

    /**
     * Returns the start timestamp of this time bucket.
     *
     * @return bucket start time
     */
    OffsetDateTime getBucket();

    /**
     * Returns the severity of the first event in this bucket.
     *
     * @return first severity string
     */
    String getFirstSeverity();

    /**
     * Returns the severity of the last event in this bucket.
     *
     * @return last severity string
     */
    String getLastSeverity();

    /**
     * Returns the total number of events in this bucket.
     *
     * @return event count
     */
    Long getEventCount();

    /**
     * Returns the timestamp of the first event in this bucket.
     *
     * @return first event time
     */
    OffsetDateTime getFirstEventTime();

    /**
     * Returns the timestamp of the last event in this bucket.
     *
     * @return last event time
     */
    OffsetDateTime getLastEventTime();
}
