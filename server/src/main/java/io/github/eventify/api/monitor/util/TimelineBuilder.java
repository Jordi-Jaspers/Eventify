package io.github.eventify.api.monitor.util;

import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.TimeSpan;
import io.github.eventify.api.monitor.model.Timeline;
import io.github.eventify.api.monitor.model.TimelineDuration;
import lombok.experimental.UtilityClass;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for building timelines from events.
 * Creates a sequence of severity durations representing state changes over time.
 */
@UtilityClass
public class TimelineBuilder {

    /**
     * Builds a timeline from a list of events within a time range.
     * Events are sorted by timestamp and consecutive events with the same severity are merged.
     * If events include a "prior event" (timestamp before rangeStart), its severity is used
     * as the initial state instead of NO_DATA.
     *
     * @param events the events to build the timeline from (may include one prior event)
     * @param range  time span with start and end times (isLive determines if last duration extends to end)
     * @return a Timeline with severity durations
     */
    public Timeline fromEvents(final List<Event> events, final TimeSpan range) {
        if (events == null || events.isEmpty()) {
            return createNoDataTimeline(range.getStart(), range.getEnd());
        }

        final List<Event> sortedEvents = events.stream()
            .sorted(Comparator.comparing(Event::getTimestamp))
            .toList();

        final Event priorEvent = sortedEvents.stream()
            .filter(e -> e.getTimestamp().isBefore(range.getStart()))
            .reduce((a, b) -> b)
            .orElse(null);

        final List<Event> eventsInRange = sortedEvents.stream()
            .filter(e -> !e.getTimestamp().isBefore(range.getStart()) && !e.getTimestamp().isAfter(range.getEnd()))
            .toList();

        return buildTimeline(priorEvent, eventsInRange, range.getStart(), range.getEnd(), range.isLive());
    }

    /**
     * Creates an empty timeline with a single NO_DATA duration.
     *
     * @param rangeStart start of the time range
     * @param rangeEnd   end of the time range
     * @return a Timeline with NO_DATA for the entire range
     */
    public Timeline createNoDataTimeline(final OffsetDateTime rangeStart, final OffsetDateTime rangeEnd) {
        return Timeline.builder()
            .durations(List.of(createDuration(Severity.NO_DATA, rangeStart, rangeEnd)))
            .build();
    }

    /**
     * Gets the current severity (most recent event's severity).
     * Considers both events in range and prior events.
     *
     * @param events     the events to check (may include prior event before range)
     * @param rangeStart start of the time range
     * @param rangeEnd   end of the time range
     * @return the severity of the most recent event, or null if no events
     */
    public Severity getCurrentSeverity(
        final List<Event> events,
        final OffsetDateTime rangeStart,
        final OffsetDateTime rangeEnd
    ) {
        if (events == null || events.isEmpty()) {
            return null;
        }

        final Severity rangeEventSeverity = findMostRecentEventSeverityInRange(events, rangeStart, rangeEnd);
        return rangeEventSeverity != null ? rangeEventSeverity : findPriorEventSeverity(events, rangeStart);
    }

    private Severity findMostRecentEventSeverityInRange(
        final List<Event> events,
        final OffsetDateTime rangeStart,
        final OffsetDateTime rangeEnd
    ) {
        final java.util.Optional<Event> eventInRange = events.stream()
            .filter(e -> !e.getTimestamp().isBefore(rangeStart) && !e.getTimestamp().isAfter(rangeEnd))
            .max(Comparator.comparing(Event::getTimestamp));

        return eventInRange.map(Event::getSeverity).orElse(null);
    }

    private Severity findPriorEventSeverity(final List<Event> events, final OffsetDateTime rangeStart) {
        return events.stream()
            .filter(e -> e.getTimestamp().isBefore(rangeStart))
            .max(Comparator.comparing(Event::getTimestamp))
            .map(Event::getSeverity)
            .orElse(null);
    }

    /**
     * Builds timeline from prior event and events in range.
     * If prior event exists, uses its severity for the prefix instead of NO_DATA.
     */
    private Timeline buildTimeline(
        final Event priorEvent,
        final List<Event> eventsInRange,
        final OffsetDateTime rangeStart,
        final OffsetDateTime rangeEnd,
        final boolean extendToEnd
    ) {
        final List<TimelineDuration> durations = new ArrayList<>();

        // Determine initial severity from prior event (or NO_DATA if none)
        final Severity initialSeverity = priorEvent != null ? priorEvent.getSeverity() : Severity.NO_DATA;

        // If no events in range, entire range has the initial severity
        if (eventsInRange.isEmpty()) {
            durations.add(createDuration(initialSeverity, rangeStart, rangeEnd));
            return Timeline.builder().durations(durations).build();
        }

        // Add prefix from range start to first event
        final OffsetDateTime firstEventTime = eventsInRange.getFirst().getTimestamp();
        if (firstEventTime.isAfter(rangeStart)) {
            durations.add(createDuration(initialSeverity, rangeStart, firstEventTime));
        }

        // Build durations from events, merging consecutive same-severity events
        Severity currentSeverity = null;
        OffsetDateTime durationStart = null;

        for (final Event event : eventsInRange) {
            if (currentSeverity == null) {
                currentSeverity = event.getSeverity();
                durationStart = event.getTimestamp();
            } else if (!event.getSeverity().equals(currentSeverity)) {
                durations.add(createDuration(currentSeverity, durationStart, event.getTimestamp()));
                currentSeverity = event.getSeverity();
                durationStart = event.getTimestamp();
            }
        }

        // Close final duration
        if (currentSeverity != null) {
            final OffsetDateTime endTime = extendToEnd
                ? rangeEnd
                : eventsInRange.getLast().getTimestamp();
            durations.add(createDuration(currentSeverity, durationStart, endTime));
        }

        return Timeline.builder().durations(durations).build();
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
