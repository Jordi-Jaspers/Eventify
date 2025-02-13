package org.jordijaspers.eventify.api.monitoring.service;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Consumer;

import org.jordijaspers.eventify.api.event.model.Status;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.monitoring.model.TimePoint;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineDurationResponse;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineResponse;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * The TimelineConsolidator class consolidates multiple timelines into a single timeline representing the worst status at any given moment.
 */
//TODO: simplify this class
public final class TimelineConsolidator {

    /* Private constructor to prevent instantiation */
    private TimelineConsolidator() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Calculates a timeline from a list of events.
     *
     * @param events The list of events to calculate the timeline from.
     * @return A TimelineResponse representing the calculated timeline.
     */
    public static TimelineResponse calculateTimeline(final List<EventRequest> events) {
        if (events == null || events.isEmpty()) {
            return new TimelineResponse();
        }

        final List<EventRequest> sortedEvents = new ArrayList<>(events);
        sortedEvents.sort(Comparator.comparing(EventRequest::getTimestamp));

        final List<TimelineDurationResponse> durations = new ArrayList<>();
        for (int i = 0; i < sortedEvents.size(); i++) {
            final EventRequest current = sortedEvents.get(i);
            final ZonedDateTime start = current.getTimestamp();
            final ZonedDateTime end = (i < sortedEvents.size() - 1) ? sortedEvents.get(i + 1).getTimestamp() : null;
            final Status status = current.getStatus();
            durations.add(new TimelineDurationResponse(start, end, status));
        }

        return mergeDurations(durations);
    }

    /**
     * Consolidates multiple timelines into a single timeline and sets the result using the provided setter.
     *
     * @param timelines The list of timelines to consolidate.
     * @param setter    The setter to set the consolidated timeline.
     */
    public static void consolidateTimelines(final List<TimelineResponse> timelines, final Consumer<TimelineResponse> setter) {
        setter.accept(TimelineConsolidator.consolidateTimelines(timelines));
    }

    /**
     * Consolidates multiple timelines into a single timeline.
     *
     * @param timelines The list of timelines to consolidate.
     * @return A TimelineResponse representing the consolidated timeline.
     */
    @SuppressWarnings("ReturnCount")
    public static TimelineResponse consolidateTimelines(final List<TimelineResponse> timelines) {
        if (isEmpty(timelines)) {
            return new TimelineResponse();
        }

        if (timelines.size() == 1) {
            return timelines.getFirst();
        }

        // Collect all durations from all timelines
        final List<TimelineDurationResponse> allDurations = timelines.stream()
            .flatMap(t -> t.getDurations().stream())
            .toList();

        // Create a list of time points (start and end times) with their associated status changes
        final List<TimePoint> timePoints = new ArrayList<>();
        for (final TimelineDurationResponse duration : allDurations) {
            timePoints.add(new TimePoint(duration.getStartTime(), duration.getStatus(), true));
            if (duration.getEndTime() != null) {
                timePoints.add(new TimePoint(duration.getEndTime(), duration.getStatus(), false));
            }
        }

        // Sort time points by time
        timePoints.sort(Comparator.comparing(TimePoint::getTime));

        // Process time points to merge overlapping durations
        ZonedDateTime currentIntervalStart = null;
        final List<TimelineDurationResponse> mergedDurations = new ArrayList<>();
        final Map<Status, Integer> statusCounts = new HashMap<>();
        for (final TimePoint timePoint : timePoints) {
            if (isNull(currentIntervalStart)) {
                currentIntervalStart = timePoint.getTime();
                updateStatusCounts(statusCounts, timePoint);
                continue;
            }

            final ZonedDateTime intervalStart = currentIntervalStart;
            final ZonedDateTime intervalEnd = timePoint.getTime();
            final Status worstStatus = getWorstStatus(statusCounts);
            if (nonNull(worstStatus)) {
                mergedDurations.add(new TimelineDurationResponse(intervalStart, intervalEnd, worstStatus));
            }

            currentIntervalStart = timePoint.getTime();
            updateStatusCounts(statusCounts, timePoint);
        }

        // Handle the last interval if there are active statuses
        final Status worstStatus = getWorstStatus(statusCounts);
        if (worstStatus != null) {
            mergedDurations.add(new TimelineDurationResponse(currentIntervalStart, null, worstStatus));
        }

        return mergeDurations(mergedDurations);
    }

    /**
     * Updates the status counts map based on the time point.
     *
     * @param statusCounts The map of status counts.
     * @param timePoint    The time point to process.
     */
    private static void updateStatusCounts(final Map<Status, Integer> statusCounts, final TimePoint timePoint) {
        if (timePoint.isStart()) {
            statusCounts.put(timePoint.getStatus(), statusCounts.getOrDefault(timePoint.getStatus(), 0) + 1);
        } else {
            final int count = statusCounts.getOrDefault(timePoint.getStatus(), 0) - 1;
            if (count <= 0) {
                statusCounts.remove(timePoint.getStatus());
            } else {
                statusCounts.put(timePoint.getStatus(), count);
            }
        }
    }

    /**
     * Gets the worst status from the status counts map.
     *
     * @param statusCounts The map of status counts.
     * @return The worst status.
     */
    private static Status getWorstStatus(final Map<Status, Integer> statusCounts) {
        return statusCounts.keySet().stream()
            .filter(Status::isConsiderForWorst)
            .max(Comparator.comparingInt(Status::getSeverity))
            .orElse(null);
    }

    /**
     * Merges consecutive durations with the same status.
     *
     * @param durations The list of durations to merge.
     * @return A TimelineResponse with merged durations.
     */
    private static TimelineResponse mergeDurations(final List<TimelineDurationResponse> durations) {
        if (durations.isEmpty()) {
            return new TimelineResponse();
        }

        final List<TimelineDurationResponse> merged = new ArrayList<>();
        TimelineDurationResponse current = durations.getFirst();
        for (int i = 1; i < durations.size(); i++) {
            final TimelineDurationResponse next = durations.get(i);
            if (current.getStatus() == next.getStatus() && Objects.equals(current.getEndTime(), next.getStartTime())) {
                current = new TimelineDurationResponse(
                    current.getStartTime(),
                    next.getEndTime(),
                    current.getStatus()
                );
            } else {
                merged.add(current);
                current = next;
            }
        }

        merged.add(current);
        return new TimelineResponse(merged);
    }
}
