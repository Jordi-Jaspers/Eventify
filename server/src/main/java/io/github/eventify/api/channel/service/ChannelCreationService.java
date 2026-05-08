package io.github.eventify.api.channel.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.DuplicateChannelNameException;
import io.github.eventify.common.exception.DuplicateChannelSlugException;
import io.github.eventify.common.util.TimeProvider;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for creating channels with consistent validation.
 * Centralizes channel creation logic to ensure uniform validation rules
 * regardless of entry point (Web UI or API).
 */
@Service
@RequiredArgsConstructor
public class ChannelCreationService {

    private final ChannelRepository channelRepository;

    /**
     * Creates and saves a personal channel (no organization).
     *
     * @param request the create request
     * @param user    the user who owns the channel
     * @return the saved channel
     * @throws DuplicateChannelNameException if name already exists for user
     * @throws DuplicateChannelSlugException if slug already exists for user
     */
    @Transactional
    public Channel createPersonalChannel(final CreateChannelRequest request, final User user) {
        validatePersonalChannelUniqueness(request, user);
        final Channel channel = buildChannel(request, user, null);
        return channelRepository.save(channel);
    }

    /**
     * Creates and saves an organization channel.
     *
     * @param request      the create request
     * @param user         the user who created the channel
     * @param organization the organization that owns the channel
     * @return the saved channel
     * @throws DuplicateChannelNameException if name already exists for organization
     * @throws DuplicateChannelSlugException if slug already exists for organization
     */
    @Transactional
    public Channel createOrganizationChannel(final CreateChannelRequest request, final User user,
        final Organization organization) {
        validateOrganizationChannelUniqueness(request, organization);
        final Channel channel = buildChannel(request, user, organization);
        return channelRepository.save(channel);
    }

    /**
     * Updates a channel's status and saves it.
     *
     * @param channel the channel to update
     * @param status  the new status
     * @return the saved channel
     */
    @Transactional
    public Channel updateStatus(final Channel channel, final ChannelStatus status) {
        channel.setStatus(status);
        channel.setUpdatedAt(TimeProvider.now());
        return channelRepository.save(channel);
    }

    private Channel buildChannel(final CreateChannelRequest request, final User user, final Organization organization) {
        final Channel channel = new Channel(request.getName(), request.getSlug(), user, organization);
        channel.setDescription(request.getDescription());
        return channel;
    }

    private void validatePersonalChannelUniqueness(final CreateChannelRequest request, final User user) {
        if (channelRepository.findByUserIdAndNameAndOrganizationIdIsNull(user.getId(), request.getName()).isPresent()) {
            throw new DuplicateChannelNameException();
        }
        if (channelRepository.existsByUserIdAndSlugAndOrganizationIdIsNull(user.getId(), request.getSlug())) {
            throw new DuplicateChannelSlugException();
        }
    }

    private void validateOrganizationChannelUniqueness(final CreateChannelRequest request,
        final Organization organization) {
        if (channelRepository.findByOrganizationIdAndName(organization.getId(), request.getName()).isPresent()) {
            throw new DuplicateChannelNameException();
        }
        if (channelRepository.existsByOrganizationIdAndSlug(organization.getId(), request.getSlug())) {
            throw new DuplicateChannelSlugException();
        }
    }
}
