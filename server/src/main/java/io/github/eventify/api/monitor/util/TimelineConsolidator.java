package io.github.eventify.api.monitor.util;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.TimePoint;
import io.github.eventify.api.monitor.model.Timeline;
import io.github.eventify.api.monitor.model.TimelineDuration;
import io.github.eventify.api.monitor.model.TimelineSource;
import lombok.experimental.UtilityClass;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for consolidating multiple timelines into a single timeline.
 * The consolidated timeline shows the worst severity at any given moment across all input timelines.
 *
 * <p>Supports consolidation at any level of the hierarchy:
 * <ul>
 * <li>Dashboard level: consolidate channels + groups</li>
 * <li>Group level: consolidate member channels</li>
 * </ul>
 *
 * <p>All entities that can be consolidated implement {@link TimelineSource}.
 */
@UtilityClass
public class TimelineConsolidator {

    /**
     * Consolidates timeline sources into a single timeline showing the worst severity at each point in time.
     * This is the primary entry point for consolidation, accepting any mix of channels, groups, or other sources.
     *
     * @param sources the timeline sources to consolidate (channels, groups, etc.)
     * @return a consolidated Timeline, or empty Timeline if input is empty
     */
    public Timeline consolidate(final List<? extends TimelineSource> sources) {
        if (sources == null || sources.isEmpty()) {
            return Timeline.empty();
        }
        final List<Timeline> timelines = sources.stream()
            .map(TimelineSource::getTimeline)
            .toList();
        return consolidateTimelines(timelines);
    }

    /**
     * Consolidates raw timelines into a single timeline showing the worst severity at each point in time.
     * Overlapping intervals are merged, with the worst (lowest priority number) severity taking precedence.
     *
     * @param timelines the timelines to consolidate
     * @return a consolidated Timeline, or empty Timeline if input is empty
     */
    public Timeline consolidateTimelines(final List<Timeline> timelines) {
        final List<Timeline> activeTimelines = filterActiveTimelines(timelines);

        if (activeTimelines.isEmpty()) {
            return Timeline.empty();
        }

        return consolidateActiveTimelines(activeTimelines);
    }

    /**
     * Consolidates active (non-empty) timelines.
     */
    private Timeline consolidateActiveTimelines(final List<Timeline> activeTimelines) {
        if (activeTimelines.size() == 1) {
            return activeTimelines.getFirst();
        }

        final List<TimePoint> timePoints = extractTimePoints(activeTimelines);
        final List<TimelineDuration> durations = processTimePoints(timePoints);

        return mergeDurations(durations);
    }

    /**
     * Filters out null timelines and timelines with no durations.
     */
    private List<Timeline> filterActiveTimelines(final List<Timeline> timelines) {
        if (timelines == null) {
            return List.of();
        }
        return timelines.stream()
            .filter(t -> t != null && t.getDurations() != null && !t.getDurations().isEmpty())
            .toList();
    }

    /**
     * Extracts all time points (start and end) from all durations in all timelines.
     */
    private List<TimePoint> extractTimePoints(final List<Timeline> timelines) {
        return timelines.stream()
            .flatMap(t -> t.getDurations().stream())
            .flatMap(d -> createTimePointsFromDuration(d).stream())
            .sorted(Comparator.comparing(TimePoint::getTime))
            .toList();
    }

    /**
     * Creates start and end time points from a duration.
     */
    private List<TimePoint> createTimePointsFromDuration(final TimelineDuration duration) {
        final List<TimePoint> points = new ArrayList<>();
        points.add(TimePoint.startOf(duration.getStartTime(), duration.getSeverity()));
        if (duration.getEndTime() != null) {
            points.add(TimePoint.endOf(duration.getEndTime(), duration.getSeverity()));
        }
        return points;
    }

    /**
     * Processes time points using a sweep line algorithm.
     * Tracks active severities and creates durations based on the worst severity at each interval.
     * Time points at the same instant are grouped and processed together.
     */
    private List<TimelineDuration> processTimePoints(final List<TimePoint> timePoints) {
        final List<TimelineDuration> durations = new ArrayList<>();
        final Map<Severity, Integer> activeSeverityCounts = new EnumMap<>(Severity.class);
        OffsetDateTime intervalStart = null;

        int i = 0;
        while (i < timePoints.size()) {
            final OffsetDateTime currentTime = timePoints.get(i).getTime();

            // Close previous interval if we have a start and active severities
            if (intervalStart != null && !currentTime.equals(intervalStart)) {
                final Severity worstSeverity = getWorstActiveSeverity(activeSeverityCounts);
                if (worstSeverity != null) {
                    durations.add(createDuration(worstSeverity, intervalStart, currentTime));
                }
            }

            // Process all time points at the same instant together
            while (i < timePoints.size() && timePoints.get(i).getTime().equals(currentTime)) {
                updateActiveCounts(activeSeverityCounts, timePoints.get(i));
                i++;
            }

            intervalStart = currentTime;
        }

        // Handle the last interval if there are still active severities
        final Severity finalWorst = getWorstActiveSeverity(activeSeverityCounts);
        if (finalWorst != null && intervalStart != null) {
            durations.add(createDuration(finalWorst, intervalStart, null));
        }

        return durations;
    }

    /**
     * Updates the count of active severities based on a time point.
     * Increments for start points, decrements for end points.
     */
    private void updateActiveCounts(final Map<Severity, Integer> counts, final TimePoint point) {
        if (point.isStart()) {
            counts.merge(point.getSeverity(), 1, Integer::sum);
        } else {
            counts.computeIfPresent(point.getSeverity(), (k, v) -> v - 1);
            counts.remove(point.getSeverity(), 0);
        }
    }

    /**
     * Gets the worst (highest priority) severity from the active severities.
     */
    private Severity getWorstActiveSeverity(final Map<Severity, Integer> counts) {
        return counts.keySet().stream()
            .filter(s -> counts.get(s) > 0)
            .min(Comparator.comparingInt(Severity::getPriority))
            .orElse(null);
    }

    /**
     * Merges consecutive durations with the same severity.
     */
    private Timeline mergeDurations(final List<TimelineDuration> durations) {
        if (durations.isEmpty()) {
            return Timeline.empty();
        }

        final List<TimelineDuration> merged = new ArrayList<>();
        TimelineDuration current = durations.getFirst();

        for (int i = 1; i < durations.size(); i++) {
            final TimelineDuration next = durations.get(i);
            if (canMerge(current, next)) {
                current = createDuration(current.getSeverity(), current.getStartTime(), next.getEndTime());
            } else {
                merged.add(current);
                current = next;
            }
        }

        merged.add(current);
        return Timeline.builder().durations(merged).build();
    }

    /**
     * Checks if two consecutive durations can be merged (same severity and adjacent times).
     */
    private boolean canMerge(final TimelineDuration current, final TimelineDuration next) {
        return current.getSeverity() == next.getSeverity()
            && Objects.equals(current.getEndTime(), next.getStartTime());
    }

    private TimelineDuration createDuration(
        final Severity severity,
        final OffsetDateTime start,
        final OffsetDateTime end
    ) {
        return new TimelineDuration()
            .setSeverity(severity)
            .setStartTime(start)
            .setEndTime(end);
    }
}
