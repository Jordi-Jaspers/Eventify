package io.github.eventify.api.event.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.request.CreateEventRequest;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.common.exception.ChannelPausedException;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import static io.github.eventify.common.exception.ApiErrorCode.CHANNEL_NOT_FOUND;

/**
 * Service for event ingestion.
 */
@Service
@RequiredArgsConstructor
public class EventIngestionService {

    private final EventRepository eventRepository;

    private final ChannelRepository channelRepository;

    /**
     * Ingests an event into a channel. Access validation is performed at controller level via @PreAuthorize.
     *
     * @param request the event creation request
     * @return the saved event entity
     */
    public Event ingestEvent(final CreateEventRequest request) {
        final Channel channel = channelRepository.findById(request.getChannelId())
            .orElseThrow(() -> new DataNotFoundException(CHANNEL_NOT_FOUND));

        if (channel.getStatus() == ChannelStatus.PAUSED) {
            throw new ChannelPausedException();
        }

        return eventRepository.save(new Event(request, channel));
    }
}
