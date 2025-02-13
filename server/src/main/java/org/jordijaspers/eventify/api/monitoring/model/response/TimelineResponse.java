package org.jordijaspers.eventify.api.monitoring.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

import org.jordijaspers.eventify.api.event.model.Status;

import static java.time.ZoneOffset.UTC;

@Data
@AllArgsConstructor
public class TimelineResponse {

    private List<TimelineDurationResponse> durations;

    /**
     * Creates a timeline with a single UNKNOWN duration for the given time window.
     *
     * @param windowStart The start of the time window
     * @param windowEnd   The end of the time window
     * @param status      The status of the duration
     */
    public TimelineResponse(final ZonedDateTime windowStart, final ZonedDateTime windowEnd, final Status status) {
        this.durations = List.of(new TimelineDurationResponse(windowStart, windowEnd, status));
    }

    /**
     * Creates a timeline with a single UNKNOWN duration for the current time.
     */
    public TimelineResponse() {
        this.durations = List.of(new TimelineDurationResponse(ZonedDateTime.now(UTC), Status.UNKNOWN));
    }

    /**
     * Get the start time of the timeline.
     *
     * @return the start time of the timeline or null if timeline is empty
     */
    public ZonedDateTime getStartTime() {
        return durations.isEmpty() ? null : durations.getFirst().getStartTime();
    }

    /**
     * Get the end time of the timeline.
     *
     * @return the end time of the timeline or null if timeline is empty
     */
    public ZonedDateTime getEndTime() {
        return durations.isEmpty() ? null : durations.getLast().getEndTime();
    }

    /**
     * Add a duration to the timeline.
     *
     * @param duration the duration to add
     */
    public void addDuration(TimelineDurationResponse duration) {
        if (duration != null) {
            this.durations.add(duration);
        }
    }

}
