package io.github.eventify.api.event.service;

import io.github.eventify.api.channel.cache.ChannelCache;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.request.BatchEventRequest;
import io.github.eventify.api.event.model.request.CreateEventRequest;
import io.github.eventify.api.event.repository.EventRepository;
import io.github.eventify.common.exception.ChannelPausedException;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.exception.ApiErrorCode.CHANNEL_NOT_FOUND;

/**
 * Service for event ingestion.
 */
@Service
@RequiredArgsConstructor
public class EventIngestionService {

    private final EventRepository eventRepository;

    private final ChannelRepository channelRepository;

    private final ChannelCache channelCache;

    /**
     * Ingests an event into a channel. Access validation is performed at controller level via @PreAuthorize.
     * Uses request-scoped cache populated by security layer to avoid duplicate database queries.
     *
     * @param request the event creation request
     * @return the saved event entity
     */
    public Event ingestEvent(final CreateEventRequest request) {
        final Channel channel = channelCache.getOrLoad(request.getChannelId(), channelRepository::findById)
            .orElseThrow(() -> new DataNotFoundException(CHANNEL_NOT_FOUND));

        if (channel.getStatus() == ChannelStatus.PAUSED) {
            throw new ChannelPausedException();
        }

        return eventRepository.save(new Event(request, channel));
    }

    /**
     * Ingests a batch of events. Access validation is performed at controller level via @PreAuthorize.
     * All-or-nothing semantics: entire batch is saved or none at all via @Transactional.
     * Uses request-scoped cache populated by security layer to avoid duplicate database queries.
     *
     * @param request the batch event request
     * @return list of saved event entities in the same order as the request
     */
    @Transactional
    public List<Event> ingestBatch(final BatchEventRequest request) {
        final List<CreateEventRequest> eventRequests = request.getEvents();
        final Set<Long> requiredChannelIds = eventRequests.stream()
            .map(CreateEventRequest::getChannelId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        final Map<Long, Channel> channelMap = channelCache.getAllOrLoad(requiredChannelIds, channelRepository::findAllById);

        validateNoChannelsPaused(channelMap);
        return eventRepository.saveAll(
            eventRequests.stream()
                .map(eventRequest -> toEvent(eventRequest, channelMap))
                .toList()
        );
    }

    private void validateNoChannelsPaused(final Map<Long, Channel> channelMap) {
        final boolean anyPaused = channelMap.values().stream()
            .anyMatch(channel -> channel.getStatus() == ChannelStatus.PAUSED);

        if (anyPaused) {
            throw new ChannelPausedException();
        }
    }

    private Event toEvent(final CreateEventRequest request, final Map<Long, Channel> channelMap) {
        return new Event(request, channelMap.get(request.getChannelId()), request.getTimestamp());
    }
}
