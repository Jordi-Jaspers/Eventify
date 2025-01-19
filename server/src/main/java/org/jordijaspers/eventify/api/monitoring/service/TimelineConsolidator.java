package org.jordijaspers.eventify.api.monitoring.service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.jordijaspers.eventify.api.event.model.Status;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineDurationResponse;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineResponse;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * The TimelineConsolidator class consolidates multiple timelines into a single timeline representing the worst status at any given moment.
 */
public final class TimelineConsolidator {

    /* Private constructor to prevent instantiation */
    private TimelineConsolidator() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Consolidates multiple timelines into a single timeline representing the worst status at any given moment.
     *
     * @param timelines List of timelines to consolidate
     * @param setter    Consumer to set the consolidated timeline
     */
    public static void consolidateAndSetTimeline(final List<TimelineResponse> timelines, final Consumer<TimelineResponse> setter) {
        setter.accept(TimelineConsolidator.consolidateTimelines(timelines));
    }

    /**
     * Consolidates multiple timelines into a single timeline representing the worst status at any given moment.
     *
     * @param timelines List of timelines to consolidate
     * @return A new timeline containing the consolidated status durations
     */
    public static TimelineResponse consolidateTimelines(final List<TimelineResponse> timelines) {
        final ZonedDateTime startTime = timelines.stream()
            .map(TimelineResponse::getStartTime)
            .filter(Objects::nonNull)
            .min(ZonedDateTime::compareTo)
            .orElse(null);

        final ZonedDateTime endTime = timelines.stream()
            .map(TimelineResponse::getEndTime)
            .filter(Objects::nonNull)
            .max(ZonedDateTime::compareTo)
            .orElse(null);

        if (isNull(startTime) || isNull(endTime)) {
            return new TimelineResponse();
        }

        final TreeSet<ZonedDateTime> timePoints = new TreeSet<>();
        timePoints.add(startTime);
        timePoints.add(endTime);

        timelines.forEach(timeline -> timeline.getDurations().forEach(duration -> {
            timePoints.add(duration.getStartTime());
            if (nonNull(duration.getEndTime())) {
                timePoints.add(duration.getEndTime());
            }
        }));

        final TimelineResponse consolidatedTimeline = new TimelineResponse();

        ZonedDateTime previousTimePoint = null;
        for (final ZonedDateTime timePoint : timePoints) {
            if (nonNull(previousTimePoint)) {
                final Status worstStatus = calculateWorstStatusAtTime(timelines, previousTimePoint);
                consolidatedTimeline.addDuration(new TimelineDurationResponse(previousTimePoint, timePoint, worstStatus));
            }
            previousTimePoint = timePoint;
        }

        return consolidatedTimeline;
    }

    /**
     * Process a batch of events into a timeline where only status changes introduce a new durations.
     */
    public static TimelineResponse calculateTimeline(final List<EventRequest> events) {
        if (isEmpty(events)) {
            return null;
        }

        final List<TimelineDurationResponse> durations = events.stream()
            .reduce(
                new ArrayList<>(),
                (accumulator, event) -> {
                    final TimelineDurationResponse lastDuration = isNotEmpty(accumulator)
                        ? accumulator.getLast()
                        : null;

                    if (shouldCreateNewDuration(lastDuration, event)) {
                        accumulator.add(new TimelineDurationResponse(event.getTimestamp(), event.getStatus()));
                    }

                    return accumulator;
                },
                (a, b) -> {
                    throw new IllegalStateException("Parallel processing not supported");
                }
            );

        return new TimelineResponse(durations);
    }

    /**
     * Calculates the worst status across all timelines at a specific point in time.
     *
     * @param timelines List of timelines to evaluate
     * @param timePoint The point in time to evaluate
     * @return The worst status at the given time point
     */
    private static Status calculateWorstStatusAtTime(final List<TimelineResponse> timelines, final ZonedDateTime timePoint) {
        return timelines.stream()
            .map(timeline -> findStatusAtTime(timeline, timePoint))
            .reduce(Status.OK, Status::worst);
    }

    /**
     * Finds the status of a single timeline at a specific point in time.
     *
     * @param timeline  The timeline to evaluate
     * @param timePoint The point in time to evaluate
     * @return The status at the given time point
     */
    private static Status findStatusAtTime(final TimelineResponse timeline, final ZonedDateTime timePoint) {
        return timeline.getDurations().stream()
            .filter(duration -> isTimeInDuration(duration, timePoint))
            .findFirst()
            .map(TimelineDurationResponse::getStatus)
            .orElse(Status.UNKNOWN);
    }

    /**
     * Checks if a given time point falls within a duration.
     *
     * @param duration  The duration to check
     * @param timePoint The point in time to check
     * @return true if the time point is within the duration
     */
    private static boolean isTimeInDuration(final TimelineDurationResponse duration, final ZonedDateTime timePoint) {
        return !timePoint.isBefore(duration.getStartTime()) && (isNull(duration.getEndTime()) || !timePoint.isAfter(duration.getEndTime()));
    }

    /**
     * Indicates if a new duration should be created based on the last duration and the current event.
     */
    private static boolean shouldCreateNewDuration(final TimelineDurationResponse lastDuration, final EventRequest event) {
        return isNull(lastDuration) || !lastDuration.getStatus().equals(event.getStatus());
    }
}
