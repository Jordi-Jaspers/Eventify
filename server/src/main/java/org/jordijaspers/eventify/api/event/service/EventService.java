package org.jordijaspers.eventify.api.event.service;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
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

    private static final Duration STORAGE_THRESHOLD = Duration.ofDays(30);

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
     * Processes and stores a batch of events.
     *
     * @param events  the events to store
     * @param checkId the check id to store the events for
     */
    @Async("eventStorageExecutor")
    public void storeEventBatch(final List<EventRequest> events, final Long checkId) {
        final Event lastStoredEvent = getLastStoredEvent(checkId);

        // TODO: Maybe better to just store all events and delete unnecessary ones afterwards
        final List<Event> eventsToStore = filterEventsForStorage(events, lastStoredEvent);
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
    public List<Event> filterEventsForStorage(final List<EventRequest> sortedEvents, final Event lastStoredEvent) {
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
     * Retrieves the last stored event for the provided check id.
     *
     * @param checkId the check id to retrieve the last stored event for
     * @return the last stored event for the provided check id
     */
    public Event getLastStoredEvent(final Long checkId) {
        return eventRepository.findLastEventForCheckId(checkId).orElse(null);
    }

    private boolean shouldStoreEvent(final EventRequest currentEvent, final EventRequest previousEvent) {
        if (currentEvent.getStatus().isNotOk()) {
            return true;
        }

        final boolean statusChanged = previousEvent.getStatus().isNotOk();
        final boolean exceededTimeThreshold = Duration.between(
            previousEvent.getTimestamp(),
            currentEvent.getTimestamp()
        ).compareTo(STORAGE_THRESHOLD) >= 0;

        return statusChanged || exceededTimeThreshold;
    }
}
