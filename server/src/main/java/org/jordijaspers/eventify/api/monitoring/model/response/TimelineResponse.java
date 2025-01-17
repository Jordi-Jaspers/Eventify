package org.jordijaspers.eventify.api.monitoring.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimelineResponse {

    private List<TimelineDurationResponse> durations = new ArrayList<>();

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
