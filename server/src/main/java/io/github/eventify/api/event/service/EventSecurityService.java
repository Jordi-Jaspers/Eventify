package io.github.eventify.api.event.service;

import io.github.eventify.api.channel.service.ChannelSecurityService;
import io.github.eventify.api.event.model.EventMetaData;
import io.github.eventify.api.organization.service.OrganizationSecurityService;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * Security service for event search access control.
 * Bean name "eventSecurity" for use in SpEL expressions with @PreAuthorize.
 */
@Service("eventSecurity")
@RequiredArgsConstructor
public class EventSecurityService {

    private final ChannelSecurityService channelSecurityService;

    private final OrganizationSecurityService organizationSecurityService;

    /**
     * Check if user can search events in their personal channels.
     *
     * @param input     the search input
     * @param principal the user principal
     * @return true if user can search, false otherwise
     */
    public boolean canSearchUserEvents(final SortablePageInput input, final UserTokenPrincipal principal) {
        if (principal == null || input == null) {
            return false;
        }

        final Long channelId = extractChannelId(input);

        // No channel filter - allow (will return empty if no personal channels)
        // Otherwise check channel access
        return channelId == null || channelSecurityService.canAccessChannelAsUser(channelId, principal);
    }

    /**
     * Check if user can search events in organization channels.
     *
     * @param orgId     the organization ID
     * @param input     the search input
     * @param principal the user principal
     * @return true if user can search, false otherwise
     */
    public boolean canSearchOrganizationEvents(final Long orgId, final SortablePageInput input,
        final UserTokenPrincipal principal) {
        if (!hasValidOrganizationContext(orgId, input, principal)) {
            return false;
        }

        final Long channelId = extractChannelId(input);

        // No channel filter - allow (will return all org channels)
        // Otherwise check channel access in organization
        return channelId == null || channelSecurityService.canAccessChannelInOrganization(channelId, orgId, principal);
    }

    /**
     * Validates that the organization context is valid and user is a member.
     *
     * @param orgId     the organization ID
     * @param input     the search input
     * @param principal the user principal
     * @return true if context is valid and user is a member
     */
    private boolean hasValidOrganizationContext(final Long orgId, final SortablePageInput input,
        final UserTokenPrincipal principal) {
        return principal != null
            && orgId != null
            && input != null
            && organizationSecurityService.isMember(orgId, principal.getUser().getId());
    }

    /**
     * Extracts channelId from search inputs.
     *
     * @param input the search input
     * @return the channel ID or null if not present
     */
    private Long extractChannelId(final SortablePageInput input) {
        return input.getSearchInputs().stream()
            .filter(searchInput -> EventMetaData.CHANNEL_ID_TERM.equals(searchInput.getFieldName()))
            .findFirst()
            .map(SearchInput::getTextValue)
            .map(Long::parseLong)
            .orElse(null);
    }
}
