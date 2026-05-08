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
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.exception.ApiErrorCode.CHANNEL_NOT_FOUND;

/**
 * Service for event ingestion.
 * Channels are resolved by slug within the principal's scope (userId or orgId).
 * Security layer (@PreAuthorize) runs first and caches resolved channels.
 */
@Service
@RequiredArgsConstructor
public class EventIngestionService {

    private final EventRepository eventRepository;

    private final ChannelRepository channelRepository;

    private final ChannelCache channelCache;

    /**
     * Ingests a single event. Channel resolved from cache (populated by security layer) or DB.
     */
    public Event ingestEvent(final CreateEventRequest request, final ApiKeyPrincipal principal) {
        final Channel channel = resolveChannel(request.getSlug(), principal);
        validateChannelActive(channel);
        return eventRepository.save(new Event(request, channel));
    }

    /**
     * Ingests a batch of events. All-or-nothing semantics via @Transactional.
     */
    @Transactional
    public List<Event> ingestBatch(final BatchEventRequest request, final ApiKeyPrincipal principal) {
        final Map<String, Channel> channelMap = resolveChannels(request.getEvents(), principal);
        channelMap.values().forEach(this::validateChannelActive);

        return eventRepository.saveAll(
            request.getEvents().stream()
                .map(
                    eventRequest -> new Event(
                        eventRequest,
                        channelMap.get(eventRequest.getSlug()),
                        eventRequest.getTimestamp()
                    )
                )
                .toList()
        );
    }

    private Channel resolveChannel(final String slug, final ApiKeyPrincipal principal) {
        return channelCache.getBySlug(slug)
            .orElseGet(() -> resolveFromDatabase(slug, principal));
    }

    private Map<String, Channel> resolveChannels(final List<CreateEventRequest> requests,
        final ApiKeyPrincipal principal) {
        final Set<String> slugs = requests.stream()
            .map(CreateEventRequest::getSlug)
            .collect(Collectors.toSet());

        return slugs.stream()
            .collect(Collectors.toMap(slug -> slug, slug -> resolveChannel(slug, principal)));
    }

    private Channel resolveFromDatabase(final String slug, final ApiKeyPrincipal principal) {
        return channelRepository.findBySlugAndPrincipal(slug, principal)
            .orElseThrow(() -> new DataNotFoundException(CHANNEL_NOT_FOUND));
    }

    private void validateChannelActive(final Channel channel) {
        if (channel.getStatus() == ChannelStatus.PAUSED) {
            throw new ChannelPausedException();
        }
    }
}
