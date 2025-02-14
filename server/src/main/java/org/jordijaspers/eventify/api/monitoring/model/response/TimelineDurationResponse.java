package org.jordijaspers.eventify.api.monitoring.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

import org.jordijaspers.eventify.api.event.model.Status;

@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class TimelineDurationResponse {

    private ZonedDateTime startTime;

    private ZonedDateTime endTime;

    private Status status;

    /**
     * Create a new timeline duration which is still running.
     *
     * @param startTime The start time of the duration
     * @param status    The status of the duration
     */
    public TimelineDurationResponse(final ZonedDateTime startTime, final Status status) {
        this.startTime = startTime;
        this.status = status;
    }

    /**
     * Create a new timeline duration which has ended.
     *
     * @param startTime The start time of the duration
     * @param endTime   The end time of the duration
     * @param status    The status of the duration
     */
    public TimelineDurationResponse(final ZonedDateTime startTime, final ZonedDateTime endTime, final Status status) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
}
