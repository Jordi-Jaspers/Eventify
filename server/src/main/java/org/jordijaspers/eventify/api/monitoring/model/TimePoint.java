package org.jordijaspers.eventify.api.monitoring.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

import org.jordijaspers.eventify.api.event.model.Status;

/**
 * Represents a time point (start or end) for a duration.
 */
@Getter
@RequiredArgsConstructor
public class TimePoint {

    private final ZonedDateTime time;
    private final Status status;
    private final boolean isStart;

}
