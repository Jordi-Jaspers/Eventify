package io.github.eventify.api.monitor.util;

import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.TimelineDuration;
import lombok.experimental.UtilityClass;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for building duration lists from events.
 * Provides common operations for converting events to timeline durations.
 */
@UtilityClass
public class DurationBuilder {

    /**
     * Builds a list of durations from sorted events.
     * Consecutive events with the same severity are grouped into a single duration.
     * A new duration starts only when the severity changes.
     * The last duration has a null end time (live/ongoing).
     *
     * @param sortedEvents events sorted by timestamp
     * @return list of durations
     */
    public List<TimelineDuration> fromEvents(final List<Event> sortedEvents) {
        final List<TimelineDuration> durations = new ArrayList<>();

        if (sortedEvents.isEmpty()) {
            return durations;
        }

        Severity currentSeverity = sortedEvents.getFirst().getSeverity();
        OffsetDateTime durationStart = sortedEvents.getFirst().getTimestamp();

        for (int i = 1; i < sortedEvents.size(); i++) {
            final Event event = sortedEvents.get(i);

            // When severity changes, close the current duration and start a new one
            if (event.getSeverity() != currentSeverity) {
                durations.add(TimelineDuration.of(currentSeverity, durationStart, event.getTimestamp()));
                currentSeverity = event.getSeverity();
                durationStart = event.getTimestamp();
            }
        }

        // Add the final duration (ongoing, so endTime is null)
        durations.add(TimelineDuration.of(currentSeverity, durationStart, null));

        return durations;
    }

    /**
     * Builds durations from events, filtering to only include those before a boundary.
     * Consecutive events with the same severity are grouped into a single duration.
     * Durations are cut off at the boundary if they extend past it.
     *
     * @param sortedEvents events sorted by timestamp
     * @param boundary     the timestamp boundary
     * @return list of durations ending before or at the boundary
     */
    public List<TimelineDuration> fromEventsEndingBefore(
        final List<Event> sortedEvents,
        final OffsetDateTime boundary
    ) {
        final List<TimelineDuration> durations = new ArrayList<>();

        // Filter to events before boundary
        final List<Event> eventsBeforeBoundary = sortedEvents.stream()
            .filter(e -> e.getTimestamp().isBefore(boundary))
            .toList();

        if (eventsBeforeBoundary.isEmpty()) {
            return durations;
        }

        Severity currentSeverity = eventsBeforeBoundary.getFirst().getSeverity();
        OffsetDateTime durationStart = eventsBeforeBoundary.getFirst().getTimestamp();

        for (int i = 1; i < eventsBeforeBoundary.size(); i++) {
            final Event event = eventsBeforeBoundary.get(i);

            // When severity changes, close the current duration and start a new one
            if (event.getSeverity() != currentSeverity) {
                durations.add(TimelineDuration.of(currentSeverity, durationStart, event.getTimestamp()));
                currentSeverity = event.getSeverity();
                durationStart = event.getTimestamp();
            }
        }

        // Add the final duration, cut off at the boundary
        durations.add(TimelineDuration.of(currentSeverity, durationStart, boundary));

        return durations;
    }

    /**
     * Builds durations from events, filtering to only include those starting after a boundary.
     * Consecutive events with the same severity are grouped into a single duration.
     * The last duration has null end time (live/ongoing).
     *
     * @param sortedEvents events sorted by timestamp
     * @param boundary     the timestamp boundary
     * @return list of durations starting after the boundary
     */
    public List<TimelineDuration> fromEventsStartingAfter(
        final List<Event> sortedEvents,
        final OffsetDateTime boundary
    ) {
        final List<TimelineDuration> durations = new ArrayList<>();

        final List<Event> eventsAfterBoundary = sortedEvents.stream()
            .filter(e -> e.getTimestamp().isAfter(boundary))
            .toList();

        if (eventsAfterBoundary.isEmpty()) {
            return durations;
        }

        Severity currentSeverity = eventsAfterBoundary.getFirst().getSeverity();
        OffsetDateTime durationStart = eventsAfterBoundary.getFirst().getTimestamp();

        for (int i = 1; i < eventsAfterBoundary.size(); i++) {
            final Event event = eventsAfterBoundary.get(i);

            // When severity changes, close the current duration and start a new one
            if (event.getSeverity() != currentSeverity) {
                durations.add(TimelineDuration.of(currentSeverity, durationStart, event.getTimestamp()));
                currentSeverity = event.getSeverity();
                durationStart = event.getTimestamp();
            }
        }

        // Add the final duration (ongoing, so endTime is null)
        durations.add(TimelineDuration.of(currentSeverity, durationStart, null));

        return durations;
    }

    /**
     * Creates a NO_DATA duration from channel creation to first event.
     *
     * @param channelCreatedAt the channel's creation timestamp
     * @param firstEventTime   the timestamp of the first event
     * @return a NO_DATA duration
     */
    public TimelineDuration createNoDataDuration(
        final OffsetDateTime channelCreatedAt,
        final OffsetDateTime firstEventTime
    ) {
        return TimelineDuration.of(Severity.NO_DATA, channelCreatedAt, firstEventTime);
    }



    /**
     * Sorts events by timestamp.
     *
     * @param events the events to sort
     * @return a new list of events sorted by timestamp
     */
    public List<Event> sortByTimestamp(final List<Event> events) {
        return events.stream()
            .sorted(Comparator.comparing(Event::getTimestamp))
            .toList();
    }
}
