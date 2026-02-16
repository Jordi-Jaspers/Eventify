package io.github.eventify.api.channel.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.repository.OrganizationRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.repository.UserRepository;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.exception.ApiErrorCode.ORGANIZATION_NOT_FOUND_ERROR;
import static io.github.eventify.common.exception.ApiErrorCode.USER_NOT_FOUND_ERROR;

/**
 * Service for channel management via API key authentication.
 * Handles both personal and organization channel operations.
 */
@Service
@RequiredArgsConstructor
public class ApiChannelService {

    private final ChannelCreationService channelCreationService;

    private final UserRepository userRepository;

    private final OrganizationRepository organizationRepository;

    /**
     * Creates a channel via API key authentication.
     * For personal API keys, creates a personal channel.
     * For organization API keys, creates an organization channel.
     *
     * @param request   the create request
     * @param principal the API key principal
     * @return the created channel
     */
    @Transactional
    public Channel createChannel(final CreateChannelRequest request, final ApiKeyPrincipal principal) {
        final User user = userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new DataNotFoundException(USER_NOT_FOUND_ERROR));

        if (principal.isOrganizationKey()) {
            final Organization organization = organizationRepository.findById(principal.getOrganizationId())
                .orElseThrow(() -> new DataNotFoundException(ORGANIZATION_NOT_FOUND_ERROR));
            return channelCreationService.createOrganizationChannel(request, user, organization);
        }

        return channelCreationService.createPersonalChannel(request, user);
    }
}
