package io.github.eventify.api.channel.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * Security service for channel access control.
 * Bean name "channelSecurity" for use in SpEL expressions with @PreAuthorize.
 *
 * <p>Returns boolean for ownership checks - Spring Security handles 403 when false.
 * Throws DataNotFoundException for missing channels (404).
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
     * {@code @PreAuthorize("@channelSecurity.canAccess(#request.channelId, principal)")}
     * </pre>
     *
     * @param channelId the channel ID to access
     * @param principal the API key principal attempting access
     * @return true if access is granted, false if denied
     * @throws DataNotFoundException if channel not found or pending deletion
     */
    public boolean canAccess(final Long channelId, final ApiKeyPrincipal principal) {
        if (principal == null) {
            return false;
        }

        final Channel channel = channelRepository.findActiveChannelById(channelId)
            .orElseThrow(() -> new DataNotFoundException(CHANNEL_RESOURCE));

        return hasOwnership(principal, channel);
    }

    private boolean hasOwnership(final ApiKeyPrincipal principal, final Channel channel) {
        final boolean isPersonalKey = principal.getOrganizationId() == null;
        final boolean isPersonalChannel = channel.getOrganization() == null;
        if (isPersonalKey != isPersonalChannel) {
            return false;
        }

        return isPersonalKey
            ? principal.getUserId().equals(channel.getUser().getId())
            : principal.getOrganizationId().equals(channel.getOrganization().getId());
    }
}
