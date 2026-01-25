package io.github.eventify.api.monitor.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Represents a time span with start and end times.
 * Live mode is derived from whether end time is recent (within 1 minute of now).
 */
@Getter
@AllArgsConstructor
public class TimeSpan {

    private static final long LIVE_THRESHOLD_SECONDS = 60;

    private final OffsetDateTime start;
    private final OffsetDateTime end;

    /**
     * Returns true if this is a live time span (end is within 1 minute of now).
     */
    public boolean isLive() {
        return Duration.between(end, OffsetDateTime.now()).abs().toSeconds() < LIVE_THRESHOLD_SECONDS;
    }
}
