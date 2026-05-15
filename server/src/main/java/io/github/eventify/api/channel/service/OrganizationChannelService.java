package io.github.eventify.api.channel.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelMetaData;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.request.UpdateChannelRequest;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.service.OrganizationService;
import io.github.eventify.common.exception.DuplicateChannelNameException;
import io.github.eventify.common.util.TimeProvider;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.api.channel.model.ChannelMetaData.ORGANIZATION_TERM;
import static io.github.eventify.common.exception.ApiErrorCode.CHANNEL_NOT_FOUND;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;

/**
 * Service for managing organization channels.
 */
@Service
@RequiredArgsConstructor
public class OrganizationChannelService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ChannelRepository channelRepository;

    private final ChannelMetaData channelMetaData;

    private final OrganizationService organizationService;

    private final ChannelCreationService channelCreationService;

    /**
     * Creates a new organization channel.
     *
     * @param organizationId the organization ID
     * @param request        the create request
     * @return the created channel
     */
    @Transactional
    public Channel createOrganizationChannel(final Long organizationId, final CreateChannelRequest request) {
        final Organization organization = organizationService.findOrganizationById(organizationId);
        return channelCreationService.createOrganizationChannel(request, getLoggedInUser(), organization);
    }

    /**
     * Searches organization channels with pagination, filtering, and sorting.
     *
     * @param organizationId the organization ID
     * @param input          the sortable page input containing search parameters
     * @return page of channels matching the search criteria
     */
    @Transactional(readOnly = true)
    public Page<Channel> searchOrganizationChannels(final Long organizationId, final SortablePageInput input) {
        organizationService.findOrganizationById(organizationId);
        final SearchInput orgInput = new SearchInput();
        orgInput.setFieldName(ORGANIZATION_TERM);
        orgInput.setTextValue(organizationId.toString());
        input.addSearchInput(orgInput);

        final Sort sort = channelMetaData.toSort(input.getSortOrder());
        final int pageSize = input.getPageSize() > 0 ? input.getPageSize() : DEFAULT_PAGE_SIZE;
        final Pageable pageable = PageRequest.of(input.getPageNumber(), pageSize, sort);

        final Specification<Channel> specification = channelMetaData.toOrganizationChannelSpecification(input);
        return channelRepository.findAll(specification, pageable);
    }

    /**
     * Gets an organization channel by ID.
     *
     * @param organizationId the organization ID
     * @param channelId      the channel ID
     * @return the channel
     * @throws DataNotFoundException if channel not found or not in organization
     */
    @Transactional(readOnly = true)
    public Channel getOrganizationChannel(final Long organizationId, final Long channelId) {
        // Verify organization exists
        organizationService.findOrganizationById(organizationId);

        return channelRepository.findByIdAndOrganizationIdAndStatusNot(
            channelId,
            organizationId,
            ChannelStatus.PENDING_DELETION
        ).orElseThrow(() -> new DataNotFoundException(CHANNEL_NOT_FOUND));
    }

    /**
     * Updates an organization channel.
     *
     * @param organizationId the organization ID
     * @param channelId      the channel ID
     * @param request        the update request
     * @return the updated channel
     * @throws DataNotFoundException         if channel not found or not in organization
     * @throws DuplicateChannelNameException if new name already exists
     */
    @Transactional
    public Channel updateOrganizationChannel(final Long organizationId, final Long channelId, final UpdateChannelRequest request) {
        final Channel channel = getOrganizationChannel(organizationId, channelId);

        // Check for duplicate name (excluding current channel)
        final Optional<Channel> existing = channelRepository.findByOrganizationIdAndName(
            organizationId,
            request.getName()
        );
        if (existing.isPresent() && !existing.get().getId().equals(channelId)) {
            throw new DuplicateChannelNameException();
        }

        // Update fields
        channel.setName(request.getName());
        channel.setDescription(request.getDescription());
        channel.setUpdatedAt(TimeProvider.now());

        return channelRepository.save(channel);
    }

    /**
     * Batch pauses organization channels (idempotent).
     * All channels must exist and belong to the organization; otherwise throws DataNotFoundException.
     *
     * @param organizationId the organization ID
     * @param channelIds     the channel IDs to pause
     * @throws DataNotFoundException if any channel is not found or not in organization
     */
    @Transactional
    public void batchPauseOrganizationChannels(final Long organizationId, final List<Long> channelIds) {
        batchUpdateStatus(organizationId, channelIds, ChannelStatus.PAUSED);
    }

    /**
     * Batch resumes organization channels (idempotent).
     * All channels must exist and belong to the organization; otherwise throws DataNotFoundException.
     *
     * @param organizationId the organization ID
     * @param channelIds     the channel IDs to resume
     * @throws DataNotFoundException if any channel is not found or not in organization
     */
    @Transactional
    public void batchResumeOrganizationChannels(final Long organizationId, final List<Long> channelIds) {
        batchUpdateStatus(organizationId, channelIds, ChannelStatus.ACTIVE);
    }

    /**
     * Batch deletes organization channels (soft delete).
     * All channels must exist and belong to the organization; otherwise throws DataNotFoundException.
     * Re-deleting already-deleted channels also throws DataNotFoundException.
     *
     * @param organizationId the organization ID
     * @param channelIds     the channel IDs to delete
     * @throws DataNotFoundException if any channel is not found, not in organization, or already deleted
     */
    @Transactional
    public void batchDeleteOrganizationChannels(final Long organizationId, final List<Long> channelIds) {
        batchUpdateStatus(organizationId, channelIds, ChannelStatus.PENDING_DELETION);
    }

    private void batchUpdateStatus(final Long organizationId, final List<Long> channelIds, final ChannelStatus status) {
        final List<Channel> channels = loadOrgChannelsOrNotFound(organizationId, channelIds);
        channels.forEach(channel -> channelCreationService.updateStatus(channel, status));
    }

    private List<Channel> loadOrgChannelsOrNotFound(final Long organizationId, final List<Long> channelIds) {
        final List<Channel> channels = channelRepository.findActiveByIdInAndOrganizationId(channelIds, organizationId);
        if (channels.size() != channelIds.size()) {
            throw new DataNotFoundException(CHANNEL_NOT_FOUND);
        }
        return channels;
    }
}
