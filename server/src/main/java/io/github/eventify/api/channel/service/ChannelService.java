package io.github.eventify.api.channel.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.ChannelMetaData;
import io.github.eventify.api.channel.model.ChannelStatus;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.request.UpdateChannelRequest;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.common.exception.DuplicateChannelNameException;
import io.github.eventify.common.util.TimeProvider;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.api.authentication.model.Permission.MANAGE_USERS;
import static io.github.eventify.api.channel.model.ChannelMetaData.USER_TERM;
import static io.github.eventify.common.exception.ApiErrorCode.CHANNEL_NOT_FOUND;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;
import static io.github.eventify.common.security.SecurityUtil.hasAuthority;

/**
 * Service for managing user channels via web UI.
 */
@Service
@RequiredArgsConstructor
public class ChannelService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ChannelRepository channelRepository;

    private final ChannelMetaData channelMetaData;

    private final ChannelCreationService channelCreationService;

    /**
     * Creates a new personal channel for the logged-in user.
     *
     * @param request the create request
     * @return the created channel
     */
    @Transactional
    public Channel createUserChannel(final CreateChannelRequest request) {
        return channelCreationService.createPersonalChannel(request, getLoggedInUser());
    }

    /**
     * Searches personal channels for the logged-in user with pagination, filtering, and sorting.
     *
     * @param input the sortable page input containing search parameters
     * @return page of channels matching the search criteria
     */
    @Transactional(readOnly = true)
    public Page<Channel> searchUserChannels(final SortablePageInput input) {
        final User user = getLoggedInUser();
        final SearchInput userInput = new SearchInput();
        userInput.setFieldName(USER_TERM);
        userInput.setTextValue(user.getId().toString());
        input.addSearchInput(userInput);

        final Sort sort = channelMetaData.toSort(input.getSortOrder());
        final int pageSize = input.getPageSize() > 0 ? input.getPageSize() : DEFAULT_PAGE_SIZE;
        final Pageable pageable = PageRequest.of(input.getPageNumber(), pageSize, sort);

        final Specification<Channel> specification = channelMetaData.toUserChannelSpecification(input);
        return channelRepository.findAll(specification, pageable);
    }

    /**
     * Gets a personal channel by ID for the logged-in user.
     *
     * @param channelId the channel ID
     * @return the channel
     * @throws DataNotFoundException if channel not found or not owned by user
     */
    @Transactional(readOnly = true)
    public Channel getUserChannel(final Long channelId) {
        final User user = getLoggedInUser();
        return channelRepository.findByIdAndUserIdAndStatusNot(
            channelId,
            user.getId(),
            ChannelStatus.PENDING_DELETION
        ).orElseThrow(() -> new DataNotFoundException(CHANNEL_NOT_FOUND));
    }

    /**
     * Gets a channel by ID without user filtering.
     * This is used by admin users who have bypassed the ownership check via @PreAuthorize.
     *
     * @param channelId the channel ID
     * @return the channel
     * @throws DataNotFoundException if channel not found
     */
    @Transactional(readOnly = true)
    public Channel getChannelById(final Long channelId) {
        return channelRepository.findActiveChannelById(channelId)
            .orElseThrow(() -> new DataNotFoundException(CHANNEL_NOT_FOUND));
    }

    /**
     * Gets a channel using admin-aware logic.
     * If caller has MANAGE_USERS authority, fetches without user filtering.
     * Otherwise, fetches only if the logged-in user owns the channel.
     *
     * @param channelId the channel ID
     * @return the channel
     * @throws DataNotFoundException if channel not found or access denied
     */
    @Transactional(readOnly = true)
    public Channel getChannelWithAdminFallback(final Long channelId) {
        if (hasAuthority(MANAGE_USERS.name())) {
            return getChannelById(channelId);
        }
        return getUserChannel(channelId);
    }

    /**
     * Updates a personal channel for the logged-in user.
     *
     * @param channelId the channel ID
     * @param request   the update request
     * @return the updated channel
     * @throws DataNotFoundException         if channel not found or not owned by user
     * @throws DuplicateChannelNameException if new name already exists
     */
    @Transactional
    public Channel updateUserChannel(final Long channelId, final UpdateChannelRequest request) {
        final Channel channel = getChannelWithAdminFallback(channelId);

        // Check for duplicate name (excluding current channel) for the channel owner
        final Optional<Channel> existing = channelRepository.findByUserIdAndNameAndOrganizationIdIsNull(
            channel.getUser().getId(),
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
     * Pauses a personal channel (idempotent).
     *
     * @param channelId the channel ID
     * @return the paused channel
     * @throws DataNotFoundException if channel not found or not owned by user
     */
    @Transactional
    public Channel pauseUserChannel(final Long channelId) {
        final Channel channel = getChannelWithAdminFallback(channelId);
        return channelCreationService.updateStatus(channel, ChannelStatus.PAUSED);
    }

    /**
     * Resumes a personal channel (idempotent).
     *
     * @param channelId the channel ID
     * @return the resumed channel
     * @throws DataNotFoundException if channel not found or not owned by user
     */
    @Transactional
    public Channel resumeUserChannel(final Long channelId) {
        final Channel channel = getChannelWithAdminFallback(channelId);
        return channelCreationService.updateStatus(channel, ChannelStatus.ACTIVE);
    }

    /**
     * Deletes a personal channel (soft delete - sets status to PENDING_DELETION).
     *
     * @param channelId the channel ID
     * @return the deleted channel
     * @throws DataNotFoundException if channel not found or not owned by user
     */
    @Transactional
    public Channel deleteUserChannel(final Long channelId) {
        final Channel channel = getChannelWithAdminFallback(channelId);
        return channelCreationService.updateStatus(channel, ChannelStatus.PENDING_DELETION);
    }
}
