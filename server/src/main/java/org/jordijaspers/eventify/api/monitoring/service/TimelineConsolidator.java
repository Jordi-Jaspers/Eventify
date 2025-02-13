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
     * Calculates a timeline from a list of events.
     *
     * @param events The list of events to calculate the timeline from.
     * @return A TimelineResponse representing the calculated timeline.
     */
    public static TimelineResponse calculateTimeline(final List<EventRequest> events) {
        if (isEmpty(events)) {
            return new TimelineResponse();
        }

        final List<TimelineDurationResponse> durations = events.stream()
            .sorted(Comparator.comparing(EventRequest::getTimestamp))
            .reduce(
                new ArrayList<>(),
                (accumulator, currentEvent) -> {
                    if (isNotEmpty(accumulator)) {
                        final TimelineDurationResponse previousDuration = accumulator.getLast();
                        previousDuration.setEndTime(currentEvent.getTimestamp());
                    }

                    accumulator.add(new TimelineDurationResponse(currentEvent.getTimestamp(), currentEvent.getStatus()));
                    return accumulator;
                },
                (list1, list2) -> {
                    list1.addAll(list2);
                    return list1;
                }
            );

        return mergeDurations(durations);
    }

    /**
     * Consolidates multiple timelines into a single timeline and sets the result using the provided setter.
     *
     * @param timelines The list of timelines to consolidate.
     * @param setter    The setter to set the consolidated timeline.
     */
    public static void consolidateTimelines(final List<TimelineResponse> timelines, final Consumer<TimelineResponse> setter) {
        setter.accept(consolidateTimelines(timelines));
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

        final List<TimePoint> timePoints = extractTimePoints(timelines);
        final List<TimelineDurationResponse> timeline = processTimePoints(timePoints);

        return mergeDurations(timeline);
    }

    /**
     * Extracts time points from all timelines.
     *
     * @param timelines The list of timelines.
     * @return A list of TimePoint objects.
     */
    private static List<TimePoint> extractTimePoints(final List<TimelineResponse> timelines) {
        return timelines.stream()
            .flatMap(t -> t.getDurations().stream())
            .flatMap(d -> createTimePointsFromDuration(d).stream())
            .sorted(Comparator.comparing(TimePoint::getTime))
            .toList();
    }

    /**
     * Creates start and end time points from a duration.
     *
     * @param duration The duration to create time points from.
     * @return A list of TimePoint objects.
     */
    private static List<TimePoint> createTimePointsFromDuration(final TimelineDurationResponse duration) {
        final List<TimePoint> timePoints = new ArrayList<>();
        timePoints.add(new TimePoint(duration.getStartTime(), duration.getStatus(), true));
        if (nonNull(duration.getEndTime())) {
            timePoints.add(new TimePoint(duration.getEndTime(), duration.getStatus(), false));
        }
        return timePoints;
    }

    /**
     * Processes time points to merge overlapping durations.
     *
     * @param timePoints The list of time points.
     * @return A list of merged TimelineDurationResponse objects.
     */
    private static List<TimelineDurationResponse> processTimePoints(final List<TimePoint> timePoints) {
        final List<TimelineDurationResponse> mergedDurations = new ArrayList<>();
        final Map<Status, Integer> statusCounts = new HashMap<>();
        ZonedDateTime currentIntervalStart = null;

        for (final TimePoint timePoint : timePoints) {
            if (isNull(currentIntervalStart)) {
                currentIntervalStart = timePoint.getTime();
                updateStatusCounts(statusCounts, timePoint);
                continue;
            }

            final Status worstStatus = getWorstStatus(statusCounts);
            if (nonNull(worstStatus)) {
                mergedDurations.add(new TimelineDurationResponse(currentIntervalStart, timePoint.getTime(), worstStatus));
            }

            currentIntervalStart = timePoint.getTime();
            updateStatusCounts(statusCounts, timePoint);
        }

        // Handle the last interval if there are active statuses
        final Status worstStatus = getWorstStatus(statusCounts);
        if (nonNull(worstStatus)) {
            mergedDurations.add(new TimelineDurationResponse(currentIntervalStart, null, worstStatus));
        }

        return mergedDurations;
    }

    /**
     * Updates the status counts map based on the time point.
     *
     * @param statusCounts The map of status counts.
     * @param timePoint    The time point to process.
     */
    private static void updateStatusCounts(final Map<Status, Integer> statusCounts, final TimePoint timePoint) {
        if (timePoint.isStart()) {
            statusCounts.merge(timePoint.getStatus(), 1, Integer::sum);
        } else {
            statusCounts.computeIfPresent(timePoint.getStatus(), (k, v) -> v - 1);
            statusCounts.remove(timePoint.getStatus(), 0);
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
        if (isEmpty(durations)) {
            return new TimelineResponse();
        }

        final List<TimelineDurationResponse> merged = new ArrayList<>();
        TimelineDurationResponse current = durations.getFirst();

        for (int i = 1; i < durations.size(); i++) {
            final TimelineDurationResponse next = durations.get(i);
            if (current.getStatus() == next.getStatus() && Objects.equals(current.getEndTime(), next.getStartTime())) {
                current = new TimelineDurationResponse(current.getStartTime(), next.getEndTime(), current.getStatus());
            } else {
                merged.add(current);
                current = next;
            }
        }

        merged.add(current);
        return new TimelineResponse(merged);
    }
}
