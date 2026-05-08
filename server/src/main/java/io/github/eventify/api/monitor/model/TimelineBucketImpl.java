package io.github.eventify.api.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * Concrete implementation of {@link TimelineBucket} for programmatic construction and testing.
 */
@Getter
@AllArgsConstructor
class TimelineBucketImpl implements TimelineBucket {

    private final Long channelId;
    private final Instant bucket;
    private final String firstSeverity;
    private final String lastSeverity;
    private final Long eventCount;
    private final Instant firstEventTime;
    private final Instant lastEventTime;
}
