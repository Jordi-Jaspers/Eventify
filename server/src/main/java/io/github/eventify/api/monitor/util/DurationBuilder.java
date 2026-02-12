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
     * Each event creates a new duration that ends when the next event starts.
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

        for (int i = 0; i < sortedEvents.size(); i++) {
            final Event current = sortedEvents.get(i);
            final OffsetDateTime endTime = (i < sortedEvents.size() - 1)
                ? sortedEvents.get(i + 1).getTimestamp()
                : null;

            durations.add(TimelineDuration.of(current.getSeverity(), current.getTimestamp(), endTime));
        }

        return durations;
    }

    /**
     * Builds durations from events, filtering to only include those before a boundary.
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

        if (sortedEvents.isEmpty()) {
            return durations;
        }

        for (int i = 0; i < sortedEvents.size(); i++) {
            final Event current = sortedEvents.get(i);

            if (current.getTimestamp().isBefore(boundary)) {
                final OffsetDateTime nextTimestamp = (i < sortedEvents.size() - 1)
                    ? sortedEvents.get(i + 1).getTimestamp()
                    : boundary;

                // Cut off at boundary if extends past it
                final OffsetDateTime endTime = nextTimestamp.isAfter(boundary) ? boundary : nextTimestamp;
                durations.add(TimelineDuration.of(current.getSeverity(), current.getTimestamp(), endTime));
            }
        }

        return durations;
    }

    /**
     * Builds durations from events, filtering to only include those starting after a boundary.
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

        if (sortedEvents.isEmpty()) {
            return durations;
        }

        final List<Event> eventsAfterBoundary = sortedEvents.stream()
            .filter(e -> e.getTimestamp().isAfter(boundary))
            .toList();

        for (int i = 0; i < eventsAfterBoundary.size(); i++) {
            final Event current = eventsAfterBoundary.get(i);
            final OffsetDateTime endTime = (i < eventsAfterBoundary.size() - 1)
                ? eventsAfterBoundary.get(i + 1).getTimestamp()
                : null;

            durations.add(TimelineDuration.of(current.getSeverity(), current.getTimestamp(), endTime));
        }

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
