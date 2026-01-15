package io.github.eventify.api.channel.service;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.common.exception.ChannelAccessDeniedException;
import io.github.eventify.common.exception.ChannelPausedException;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * Service for validating API key access to channels.
 * For programmatic validation with ApiKey entities.
 *
 * @see ChannelSecurityService for @PreAuthorize SpEL expressions
 */
@Service
@RequiredArgsConstructor
public class ChannelAccessService {

    private static final String CHANNEL_RESOURCE = "Channel";

    private final ChannelRepository channelRepository;

    /**
     * Validates that an API key can access a specific channel.
     * Throws exceptions for denied or invalid access (no return value for success).
     *
     * @param apiKey    the API key attempting access
     * @param channelId the channel ID to access
     * @throws IllegalArgumentException     if apiKey is null
     * @throws DataNotFoundException        if channelId is null, channel not found, or channel is pending deletion
     * @throws ChannelPausedException       if channel is paused
     * @throws ChannelAccessDeniedException if ownership doesn't match or cross-type access attempted
     */
    public void validateAccess(final ApiKey apiKey, final Long channelId) {
        validateApiKey(apiKey);
        final Channel channel = fetchAndValidateChannel(channelId);
        validateOwnership(apiKey, channel);
    }

    private void validateApiKey(final ApiKey apiKey) {
        if (apiKey == null) {
            throw new IllegalArgumentException("API key is required");
        }
    }

    private Channel fetchAndValidateChannel(final Long channelId) {
        final Channel channel = channelRepository.findById(channelId)
            .orElseThrow(() -> new DataNotFoundException(CHANNEL_RESOURCE));

        if (channel.getStatus() == ChannelStatus.PENDING_DELETION) {
            throw new DataNotFoundException(CHANNEL_RESOURCE);
        }

        if (channel.getStatus() == ChannelStatus.PAUSED) {
            throw new ChannelPausedException();
        }

        return channel;
    }

    private void validateOwnership(final ApiKey apiKey, final Channel channel) {
        final boolean isPersonalKey = apiKey.getOrganization() == null;
        final boolean isPersonalChannel = channel.getOrganization() == null;

        if (isPersonalKey != isPersonalChannel) {
            throw new ChannelAccessDeniedException();
        }

        if (isPersonalKey) {
            validatePersonalOwnership(apiKey, channel);
        } else {
            validateOrganizationOwnership(apiKey, channel);
        }
    }

    private void validatePersonalOwnership(final ApiKey apiKey, final Channel channel) {
        if (!apiKey.getUser().getId().equals(channel.getUser().getId())) {
            throw new ChannelAccessDeniedException();
        }
    }

    private void validateOrganizationOwnership(final ApiKey apiKey, final Channel channel) {
        if (!apiKey.getOrganization().getId().equals(channel.getOrganization().getId())) {
            throw new ChannelAccessDeniedException();
        }
    }
}
