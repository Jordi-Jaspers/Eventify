package org.jordijaspers.eventify.api.event.service;

import lombok.RequiredArgsConstructor;

import org.jordijaspers.eventify.api.event.model.Event;
import org.jordijaspers.eventify.api.event.model.EventId;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.event.repository.EventRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    /**
     * Creates an event based on the provided {@link EventRequest}.
     *
     * @param eventRequest the {@link EventRequest} to create the event from
     */
    public void createEvent(final EventRequest eventRequest) {
        final Event event = new Event();
        event.setId(new EventId(eventRequest.getCheckId(), eventRequest.getTimestamp().toLocalDateTime()));
        event.setStatus(eventRequest.getStatus());
        event.setMessage(eventRequest.getMessage());
        event.setCorrelationId(eventRequest.getCorrelationId());
        eventRepository.save(event);
    }

}
