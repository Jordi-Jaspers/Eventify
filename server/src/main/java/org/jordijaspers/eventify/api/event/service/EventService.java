package org.jordijaspers.eventify.api.event.service;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.transaction.Transactional;

import org.jordijaspers.eventify.api.event.model.Event;
import org.jordijaspers.eventify.api.event.model.mapper.EventMapper;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.event.repository.EventRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;
import static org.jordijaspers.eventify.api.event.model.Status.UNKNOWN;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private static final Duration CHECKPOINT_INTERVAL = Duration.ofDays(30);

    private final EventRepository eventRepository;

    private final EventMapper eventMapper;

    /**
     * Creates an event based on the provided {@link EventRequest}.
     *
     * @param eventRequest the {@link EventRequest} to create the event from
     */
    public void createEvent(final EventRequest eventRequest) {
        final Event event = eventMapper.toEvent(eventRequest);
        eventRepository.save(event);
    }

    /**
     * Retrieves the last stored event for the provided check id.
     *
     * @param checkId the check id to retrieve the last stored event for
     * @return the last stored event for the provided check id
     */
    public Event getRecentEventSince(final Long checkId, final LocalDateTime since) {
        return eventRepository.findRecentEventSince(checkId, since).orElse(null);
    }

    /**
     * Processes and stores a batch of events.
     *
     * @param events  the events to store
     * @param checkId the check id to store the events for
     */
    @Async("eventStorageExecutor")
    public void storeEventBatch(final List<EventRequest> events, final Long checkId) {
        final Event recentEvent = getRecentEventSince(checkId, events.getFirst().getTimestamp().toLocalDateTime());
        final List<Event> eventsToStore = determineEventsToStore(events, recentEvent);
        eventRepository.saveAll(eventsToStore);
    }

    /**
     * Filters the events in the batch to determine which events should be stored. The events will be stored if:
     * <ul>
     * <li>The status of the current event is not OK</li>
     * <li>The status of the previous event is OK and the time difference is more than the threshold</li>
     * <li>The status of the current event is different from the previous event</li>
     * </ul>
     *
     * @param sortedEvents    A sorted list of events to filter
     * @param lastStoredEvent the last stored event for the check
     * @return the events that should be stored
     */
    private List<Event> determineEventsToStore(final List<EventRequest> sortedEvents, final Event lastStoredEvent) {
        final List<Event> result = new ArrayList<>();
        EventRequest previousEvent = nonNull(lastStoredEvent)
            ? eventMapper.toEventRequest(lastStoredEvent)
            : new EventRequest(0L, UNKNOWN);

        for (final EventRequest currentEvent : sortedEvents) {
            if (shouldStoreEvent(currentEvent, previousEvent)) {
                result.add(eventMapper.toEvent(currentEvent));
                previousEvent = currentEvent;
            }
        }

        return result;
    }

    /**
     * Determines if the current event should be stored based on the previous event.
     *
     * @param currentEvent  the current event
     * @param previousEvent the previous event
     * @return true if the event should be stored, false otherwise
     */
    private boolean shouldStoreEvent(final EventRequest currentEvent, final EventRequest previousEvent) {
        return previousEvent.getStatus().equals(UNKNOWN)
            || currentEvent.getStatus().isNotOk()
            || !previousEvent.getStatus().equals(currentEvent.getStatus())
            || Duration.between(previousEvent.getTimestamp(), currentEvent.getTimestamp()).compareTo(CHECKPOINT_INTERVAL) > 0;
    }
}
