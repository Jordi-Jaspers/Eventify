package org.jordijaspers.eventify.api.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;

import org.jordijaspers.eventify.api.event.model.Event;
import org.jordijaspers.eventify.api.event.model.EventBatch;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.monitoring.service.TimelineStreamingService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventConsumer {

    private final EventService eventService;

    private final TimelineStreamingService timelineStreamingService;

    /**
     * Consume a batch of events from the RabbitMQ Queue. The event will be processed by the timeline streaming service. The event will be
     * acknowledged when the processing is successful. if the processing fails, the event will be re-queued.
     *
     * @param batch The event to consume
     */
    @RabbitListener(
        queues = "monitoring.events.queue",
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void consume(final EventBatch batch) {
        final Event lastStoredEvent = eventService.getLastStoredEvent(batch.getCheckId());
        final List<EventRequest> sortedEvents = batch.getEvents().stream()
            // TODO: Not really sure if we should only accepts events that are after the last stored event
            .filter(event -> isNull(event) || event.getTimestamp().isAfter(lastStoredEvent.getZonedTimestamp()))
            .sorted(Comparator.comparing(EventRequest::getTimestamp))
            .toList();

        timelineStreamingService.updateTimelineForCheck(sortedEvents, batch.getCheckId());
        eventService.storeEventBatch(sortedEvents, batch.getCheckId());
        log.debug("Batch of '{}' events for check '{}' has been processed.", batch.getEvents().size(), batch.getCheckId());
    }
}
