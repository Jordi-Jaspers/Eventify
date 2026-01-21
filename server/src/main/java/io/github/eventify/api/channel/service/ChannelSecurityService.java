package io.github.eventify.api.channel.service;

import io.github.eventify.api.channel.cache.ChannelCache;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.event.model.request.BatchEventRequest;
import io.github.eventify.api.event.model.request.CreateEventRequest;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

/**
 * Security service for channel access control.
 * Bean name "channelSecurity" for use in SpEL expressions with @PreAuthorize.
 *
 * <p>Returns boolean only - never throws exceptions.
 * Spring Security converts false to 403 Forbidden.
 * Non-existent channels return false (not 404) to avoid leaking resource existence.
 */
@Service("channelSecurity")
@RequiredArgsConstructor
public class ChannelSecurityService {

    private final ChannelRepository channelRepository;

    private final ChannelCache channelCache;

    /**
     * Check if an API key principal can access a specific channel.
     *
     * @param channelId the channel ID to access
     * @param principal the API key principal attempting access
     * @return true if access is granted, false if denied or channel not found
     */
    public boolean canAccess(final Long channelId, final ApiKeyPrincipal principal) {
        if (principal == null || channelId == null) {
            return false;
        }

        return channelRepository.findActiveChannelById(channelId)
            .filter(channel -> hasOwnership(principal, channel))
            .map(this::cacheAndGrantAccess)
            .orElse(false);
    }

    /**
     * Check if an API key principal can access all channels in a batch request.
     *
     * <p>Returns true for empty batches to allow validator to handle those cases.
     *
     * @param request   the batch event request containing events with channel IDs
     * @param principal the API key principal attempting access
     * @return true if access is granted to all channels, false if denied or any channel not found
     */
    public boolean canAccessBatch(final BatchEventRequest request, final ApiKeyPrincipal principal) {
        if (principal == null) {
            return false;
        }

        return isEmptyBatch(request) || hasAccessToAllChannels(request, principal);
    }

    private boolean cacheAndGrantAccess(final Channel channel) {
        channelCache.put(channel);
        return true;
    }

    private boolean isEmptyBatch(final BatchEventRequest request) {
        return request == null || request.getEvents() == null || request.getEvents().isEmpty();
    }

    private boolean hasAccessToAllChannels(final BatchEventRequest request, final ApiKeyPrincipal principal) {
        final Set<Long> channelIds = extractChannelIds(request);
        final Map<Long, Channel> channels = fetchAndCacheChannels(channelIds);

        return channels.size() == channelIds.size()
            && channels.values().stream().allMatch(channel -> hasOwnership(principal, channel));
    }

    private Set<Long> extractChannelIds(final BatchEventRequest request) {
        return request.getEvents().stream()
            .map(CreateEventRequest::getChannelId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private Map<Long, Channel> fetchAndCacheChannels(final Set<Long> channelIds) {
        if (channelIds.isEmpty()) {
            return Map.of();
        }

        final List<Channel> channels = channelRepository.findActiveChannelsByIds(channelIds);
        channelCache.putAll(channels);

        return channels.stream().collect(Collectors.toMap(Channel::getId, Function.identity()));
    }

    private boolean hasOwnership(final ApiKeyPrincipal principal, final Channel channel) {
        if (channel.getOrganization() != null) {
            return channel.getOrganization().getId().equals(principal.getOrganizationId());
        }

        return principal.getOrganizationId() == null
            && channel.getUser().getId().equals(principal.getUserId());
    }
}
