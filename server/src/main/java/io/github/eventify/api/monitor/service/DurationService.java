package io.github.eventify.api.monitor.service;

import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.api.monitor.model.DurationDirection;
import io.github.eventify.api.monitor.model.TimelineDuration;
import io.github.eventify.api.monitor.model.response.DurationDetailsResponse;
import io.github.eventify.api.monitor.util.DurationBuilder;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for fetching duration details around specific timestamps.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.GodClass")
public class DurationService {

    private static final Duration MINIMUM_DISPLAY_WINDOW = Duration.ofMinutes(15);
    private static final int MAXIMUM_DURATIONS = 10;
    private static final int FETCH_LIMIT = 50;

    private final EventRepository eventRepository;

    /**
     * Gets durations based on direction and timestamp.
     *
     * @param channelId the channel ID
     * @param timestamp the reference timestamp
     * @param direction the direction to fetch (AROUND, BEFORE, AFTER)
     * @return duration details response
     */
    @Transactional(readOnly = true)
    public DurationDetailsResponse getDurations(
        final Long channelId,
        final OffsetDateTime timestamp,
        final DurationDirection direction
    ) {
        return switch (direction) {
            case AROUND -> getDurationsAround(channelId, timestamp);
            case BEFORE -> getDurationsBefore(channelId, timestamp);
            case AFTER -> getDurationsAfter(channelId, timestamp);
        };
    }

    /**
     * Gets durations centered around a specific timestamp.
     *
     * @param channelId the channel ID
     * @param timestamp the timestamp to center around
     * @return duration details response
     */
    @Transactional(readOnly = true)
    public DurationDetailsResponse getDurationsAround(final Long channelId, final OffsetDateTime timestamp) {
        final List<Event> events = eventRepository.findEventsAroundTimestamp(channelId, timestamp, FETCH_LIMIT);

        if (events.isEmpty()) {
            return createEmptyResponse();
        }

        final List<Event> sortedEvents = DurationBuilder.sortByTimestamp(events);
        final List<TimelineDuration> allDurations = buildDurationsWithNoDataPrefix(sortedEvents);
        final int selectedIndex = findSelectedIndex(allDurations, timestamp);

        final List<TimelineDuration> windowDurations = expandWindow(
            allDurations,
            selectedIndex,
            timestamp
        );

        final boolean hasPrevious = hasEarlierDurations(windowDurations, allDurations);
        final boolean hasNext = hasLaterDurations(windowDurations, allDurations);

        final int adjustedIndex = findSelectedIndex(windowDurations, timestamp);

        return new DurationDetailsResponse()
            .setDurations(windowDurations)
            .setSelectedIndex(adjustedIndex)
            .setHasPrevious(hasPrevious)
            .setHasNext(hasNext);
    }

    /**
     * Gets durations before a specific timestamp.
     *
     * @param channelId the channel ID
     * @param timestamp the timestamp boundary
     * @return duration details response
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("checkstyle:ReturnCount")
    public DurationDetailsResponse getDurationsBefore(final Long channelId, final OffsetDateTime timestamp) {
        final List<Event> events = eventRepository.findEventsBefore(channelId, timestamp, FETCH_LIMIT);

        if (events.isEmpty()) {
            return createEmptyResponse();
        }

        final List<Event> sortedEvents = DurationBuilder.sortByTimestamp(events);
        final List<TimelineDuration> allDurations = buildDurationsWithNoDataPrefix(sortedEvents);

        // Filter to only durations that end before the timestamp (or contain it as an earlier duration)
        final List<TimelineDuration> durationsBeforeTimestamp = filterDurationsEndingBeforeOrAt(allDurations, timestamp);

        if (durationsBeforeTimestamp.isEmpty()) {
            return createEmptyResponse();
        }

        final int limit = Math.min(durationsBeforeTimestamp.size(), MAXIMUM_DURATIONS);
        final List<TimelineDuration> windowDurations = new ArrayList<>(
            durationsBeforeTimestamp.subList(
                Math.max(0, durationsBeforeTimestamp.size() - limit),
                durationsBeforeTimestamp.size()
            )
        );

        // hasPrevious is false if:
        // 1. Window shows all available durations, AND
        // 2. The first duration is NO_DATA (meaning we've reached the channel's beginning)
        final boolean atChannelStart = !windowDurations.isEmpty()
            && windowDurations.getFirst().getSeverity() == Severity.NO_DATA;
        final boolean hasMoreDurations = durationsBeforeTimestamp.size() > limit;
        final boolean hasPrevious = hasMoreDurations || !atChannelStart;

        return new DurationDetailsResponse()
            .setDurations(windowDurations)
            .setSelectedIndex(windowDurations.size() - 1)
            .setHasPrevious(hasPrevious)
            .setHasNext(true);
    }

    /**
     * Gets durations after a specific timestamp.
     *
     * @param channelId the channel ID
     * @param timestamp the timestamp boundary
     * @return duration details response
     */
    @Transactional(readOnly = true)
    public DurationDetailsResponse getDurationsAfter(final Long channelId, final OffsetDateTime timestamp) {
        final List<Event> events = eventRepository.findEventsAfter(channelId, timestamp, FETCH_LIMIT);

        if (events.isEmpty()) {
            return createEmptyResponse();
        }

        final List<Event> sortedEvents = DurationBuilder.sortByTimestamp(events);
        final List<TimelineDuration> durations = DurationBuilder.fromEventsStartingAfter(sortedEvents, timestamp);

        final int limit = Math.min(durations.size(), MAXIMUM_DURATIONS);
        final List<TimelineDuration> windowDurations = durations.subList(0, limit);

        return new DurationDetailsResponse()
            .setDurations(windowDurations)
            .setSelectedIndex(0)
            .setHasPrevious(true)
            .setHasNext(durations.size() > limit);
    }

    private DurationDetailsResponse createEmptyResponse() {
        return new DurationDetailsResponse()
            .setDurations(List.of())
            .setSelectedIndex(0)
            .setHasPrevious(false)
            .setHasNext(false);
    }

    /**
     * Builds durations from events, adding NO_DATA prefix if this is the channel's first event.
     */
    private List<TimelineDuration> buildDurationsWithNoDataPrefix(final List<Event> sortedEvents) {
        if (sortedEvents.isEmpty()) {
            return List.of();
        }

        final List<TimelineDuration> durations = new ArrayList<>(DurationBuilder.fromEvents(sortedEvents));

        // Add NO_DATA prefix for channel's first event
        final Event firstEvent = sortedEvents.getFirst();
        if (shouldAddNoDataPrefix(sortedEvents) && firstEvent.getChannel() != null) {
            final TimelineDuration noDataDuration = DurationBuilder.createNoDataDuration(
                firstEvent.getChannel().getCreatedAt(),
                firstEvent.getTimestamp()
            );
            durations.addFirst(noDataDuration);
        }

        return durations;
    }

    private boolean shouldAddNoDataPrefix(final List<Event> events) {
        // NO_DATA is needed if the first event is the channel's very first event
        // We determine this by checking if all events share the same timestamp
        final long uniqueTimestamps = events.stream()
            .map(Event::getTimestamp)
            .distinct()
            .count();

        return uniqueTimestamps == 1;
    }

    /**
     * Filters durations to only those that end before or at the given timestamp.
     * This includes durations where endTime <= timestamp, or ongoing durations that started before timestamp.
     */
    private List<TimelineDuration> filterDurationsEndingBeforeOrAt(
        final List<TimelineDuration> durations,
        final OffsetDateTime timestamp
    ) {
        final List<TimelineDuration> result = new ArrayList<>();
        for (final TimelineDuration duration : durations) {
            // Include if duration ends before or at the timestamp
            if (duration.getEndTime() != null && !duration.getEndTime().isAfter(timestamp)) {
                result.add(duration);
            } else if (duration.getStartTime().isBefore(timestamp)) {
                // Also include if duration contains the timestamp (for partial overlap)
                result.add(duration);
            }
        }
        return result;
    }

    private int findSelectedIndex(final List<TimelineDuration> durations, final OffsetDateTime timestamp) {
        for (int i = 0; i < durations.size(); i++) {
            final TimelineDuration duration = durations.get(i);
            if (containsTimestamp(duration, timestamp)) {
                return i;
            }
        }
        return 0;
    }

    private boolean containsTimestamp(final TimelineDuration duration, final OffsetDateTime timestamp) {
        final boolean afterStart = !timestamp.isBefore(duration.getStartTime());
        final boolean beforeEnd = duration.getEndTime() == null || timestamp.isBefore(duration.getEndTime());
        return afterStart && beforeEnd;
    }

    private List<TimelineDuration> expandWindow(
        final List<TimelineDuration> allDurations,
        final int selectedIndex,
        final OffsetDateTime timestamp
    ) {
        if (allDurations.isEmpty()) {
            return List.of();
        }

        int startIdx = selectedIndex;
        int endIdx = selectedIndex;

        // Expand window alternating prev/next until max durations reached
        while (true) {
            final boolean canExpandPrev = startIdx > 0;
            final boolean canExpandNext = endIdx < allDurations.size() - 1;

            if (!canExpandPrev && !canExpandNext) {
                break;
            }

            final int currentSize = (endIdx - startIdx + 1);
            if (currentSize >= MAXIMUM_DURATIONS) {
                break;
            }

            // Expand alternating: prev, next, prev, next, ...
            if (canExpandPrev && (startIdx - selectedIndex) <= (endIdx - selectedIndex)) {
                startIdx--;
            } else if (canExpandNext) {
                endIdx++;
            }
        }

        final List<TimelineDuration> windowDurations = new ArrayList<>(allDurations.subList(startIdx, endIdx + 1));

        // Cut off durations that extend before/after window boundaries
        if (!windowDurations.isEmpty()) {
            cutOffWindowBoundaries(windowDurations, timestamp);
        }

        return windowDurations;
    }

    private void cutOffWindowBoundaries(final List<TimelineDuration> durations, final OffsetDateTime center) {
        if (durations.size() <= 1) {
            return;
        }

        final OffsetDateTime windowStart = center.minus(MINIMUM_DISPLAY_WINDOW.dividedBy(2));
        final OffsetDateTime windowEnd = center.plus(MINIMUM_DISPLAY_WINDOW.dividedBy(2));
        final Duration cutoffThreshold = Duration.ofHours(1);

        // Cut off first duration if it's very long and extends across window start
        final TimelineDuration first = durations.getFirst();
        if (first.getEndTime() != null
            && first.getStartTime().isBefore(windowStart)
            && first.getEndTime().isAfter(windowStart)) {
            final Duration firstDuration = Duration.between(first.getStartTime(), first.getEndTime());
            if (firstDuration.compareTo(cutoffThreshold) > 0) {
                first.setStartTime(windowStart);
            }
        }

        // Cut off last duration if it's very long and extends across window end
        final TimelineDuration last = durations.getLast();
        if (last.getEndTime() != null
            && last.getStartTime().isBefore(windowEnd)
            && last.getEndTime().isAfter(windowEnd)) {
            final Duration lastDuration = Duration.between(last.getStartTime(), last.getEndTime());
            if (lastDuration.compareTo(cutoffThreshold) > 0) {
                last.setEndTime(windowEnd);
            }
        }
    }

    private boolean hasEarlierDurations(
        final List<TimelineDuration> windowDurations,
        final List<TimelineDuration> allDurations
    ) {
        if (windowDurations.isEmpty() || allDurations.isEmpty()) {
            return false;
        }

        // hasPrevious is false only if we're at the very start of the channel (NO_DATA present)
        return allDurations.getFirst().getSeverity() != Severity.NO_DATA;
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    private boolean hasLaterDurations(
        final List<TimelineDuration> windowDurations,
        final List<TimelineDuration> allDurations
    ) {
        if (windowDurations.isEmpty() || allDurations.isEmpty()) {
            return false;
        }

        // If last duration has null end time, it's the current/live duration
        final TimelineDuration lastWindowDuration = windowDurations.getLast();
        if (lastWindowDuration.getEndTime() == null) {
            return false;
        }

        // Check if the window ends before the last duration in all durations
        final TimelineDuration lastAllDuration = allDurations.getLast();
        final OffsetDateTime allEnd = lastAllDuration.getEndTime();

        // If all durations also ends with null (live duration), compare start times
        if (allEnd == null) {
            return !lastWindowDuration.getStartTime().equals(lastAllDuration.getStartTime());
        }

        return lastWindowDuration.getEndTime().isBefore(allEnd);
    }
}
