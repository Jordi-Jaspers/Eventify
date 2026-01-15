package io.github.eventify.api.channel.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.common.exception.ChannelAccessDeniedException;
import io.github.eventify.common.exception.ChannelPausedException;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * Security service for channel access control.
 * Bean name "channelSecurity" for use in SpEL expressions with @PreAuthorize.
 */
@Service("channelSecurity")
@RequiredArgsConstructor
public class ChannelSecurityService {

    private static final String CHANNEL_RESOURCE = "Channel";

    private final ChannelRepository channelRepository;

    /**
     * Check if an API key principal can access a specific channel.
     * For use with @PreAuthorize SpEL expressions.
     *
     * <p>Example usage:
     * <pre>
     * {@code @PreAuthorize("@channelSecurity.canAccess(#channelId, principal)")}
     * </pre>
     *
     * @param channelId the channel ID to access
     * @param principal the API key principal attempting access
     * @return true if access is granted
     * @throws DataNotFoundException        if channel not found or pending deletion
     * @throws ChannelPausedException       if channel is paused
     * @throws ChannelAccessDeniedException if ownership doesn't match
     */
    public boolean canAccess(final Long channelId, final ApiKeyPrincipal principal) {
        if (principal == null) {
            throw new ChannelAccessDeniedException();
        }
        final Channel channel = fetchAndValidateChannel(channelId);
        validateOwnership(principal, channel);
        return true;
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

    private void validateOwnership(final ApiKeyPrincipal principal, final Channel channel) {
        final boolean isPersonalKey = principal.getOrganizationId() == null;
        final boolean isPersonalChannel = channel.getOrganization() == null;

        if (isPersonalKey != isPersonalChannel) {
            throw new ChannelAccessDeniedException();
        }

        if (isPersonalKey) {
            validatePersonalOwnership(principal, channel);
        } else {
            validateOrganizationOwnership(principal, channel);
        }
    }

    private void validatePersonalOwnership(final ApiKeyPrincipal principal, final Channel channel) {
        if (!principal.getUserId().equals(channel.getUser().getId())) {
            throw new ChannelAccessDeniedException();
        }
    }

    private void validateOrganizationOwnership(final ApiKeyPrincipal principal, final Channel channel) {
        if (!principal.getOrganizationId().equals(channel.getOrganization().getId())) {
            throw new ChannelAccessDeniedException();
        }
    }
}
