package io.github.eventify.api.monitor.model;

import io.github.eventify.api.event.model.Severity;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * Represents a point in time where a severity state begins or ends.
 * Used internally by timeline utilities to track interval boundaries.
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
// PMD.OverrideBothEqualsAndHashCodeOnComparable - Lombok @EqualsAndHashCode generates both methods
@SuppressWarnings("PMD.OverrideBothEqualsAndHashCodeOnComparable")
public class TimePoint implements Comparable<TimePoint> {

    private final OffsetDateTime time;
    private final Severity severity;
    private final boolean start;

    /**
     * Creates a start point for a severity interval.
     *
     * @param time     the start time
     * @param severity the severity level
     * @return a new TimePoint marking the start of an interval
     */
    public static TimePoint startOf(final OffsetDateTime time, final Severity severity) {
        return new TimePoint(time, severity, true);
    }

    /**
     * Creates an end point for a severity interval.
     *
     * @param time     the end time
     * @param severity the severity level
     * @return a new TimePoint marking the end of an interval
     */
    public static TimePoint endOf(final OffsetDateTime time, final Severity severity) {
        return new TimePoint(time, severity, false);
    }

    /**
     * Whether this point marks the end of an interval.
     *
     * @return true if this is an end point
     */
    public boolean isEnd() {
        return !start;
    }

    @Override
    public int compareTo(final TimePoint other) {
        return this.time.compareTo(other.time);
    }
}
