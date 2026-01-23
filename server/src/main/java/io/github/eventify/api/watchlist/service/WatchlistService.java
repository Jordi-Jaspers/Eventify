package io.github.eventify.api.watchlist.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistChannel;
import io.github.eventify.api.watchlist.model.WatchlistChannelId;
import io.github.eventify.api.watchlist.model.WatchlistMetaData;
import io.github.eventify.api.watchlist.model.request.CreateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.UpdateWatchlistRequest;
import io.github.eventify.api.watchlist.repository.WatchlistChannelRepository;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.eventify.common.exception.DuplicateWatchlistNameException;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.api.watchlist.model.WatchlistMetaData.USER_TERM;
import static io.github.eventify.common.exception.ApiErrorCode.CHANNEL_NOT_FOUND;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;

/**
 * Service for managing user watchlists.
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("PMD.ExcessiveImports")
public class WatchlistService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final WatchlistRepository watchlistRepository;

    private final WatchlistChannelRepository watchlistChannelRepository;

    private final ChannelRepository channelRepository;

    private final WatchlistMetaData watchlistMetaData;

    /**
     * Creates a new personal watchlist for the logged-in user.
     *
     * @param request the create request
     * @return the created watchlist
     */
    @Transactional
    public Watchlist createWatchlist(final CreateWatchlistRequest request) {
        final User user = getLoggedInUser();

        // Check for duplicate name
        final Optional<Watchlist> existing = watchlistRepository.findByUserIdAndName(
            user.getId(),
            request.getName()
        );
        if (existing.isPresent()) {
            throw new DuplicateWatchlistNameException();
        }

        // Validate channels first (before saving watchlist)
        if (request.getChannelIds() != null && !request.getChannelIds().isEmpty()) {
            for (final Long channelId : request.getChannelIds()) {
                channelRepository.findByIdAndUserId(channelId, user.getId())
                    .orElseThrow(() -> new DataNotFoundException(CHANNEL_NOT_FOUND));
            }
        }

        // Create new watchlist
        final Watchlist watchlist = new Watchlist(request.getName(), user, null);
        watchlist.setDescription(request.getDescription());
        watchlist.setDefaultTimeRange(
            request.getDefaultTimeRange() != null ? request.getDefaultTimeRange() : "24h"
        );
        watchlist.setDefaultOnlyCritical(
            request.getDefaultOnlyCritical() != null && request.getDefaultOnlyCritical()
        );
        watchlist.setDefaultSortBySeverity(
            request.getDefaultSortBySeverity() == null || request.getDefaultSortBySeverity()
        );

        final Watchlist savedWatchlist = watchlistRepository.save(watchlist);

        // Add channels if provided
        if (request.getChannelIds() != null && !request.getChannelIds().isEmpty()) {
            addChannelsToWatchlist(savedWatchlist, request.getChannelIds(), user.getId());
        }

        return savedWatchlist;
    }

    /**
     * Searches personal watchlists for the logged-in user with pagination, filtering, and sorting.
     *
     * @param input the sortable page input containing search parameters
     * @return page of watchlists matching the search criteria
     */
    @Transactional(readOnly = true)
    public Page<Watchlist> searchWatchlists(final SortablePageInput input) {
        final User user = getLoggedInUser();
        final SearchInput userInput = new SearchInput();
        userInput.setFieldName(USER_TERM);
        userInput.setTextValue(user.getId().toString());
        input.addSearchInput(userInput);

        final Sort sort = watchlistMetaData.toSort(input.getSortOrder());
        final int pageSize = input.getPageSize() > 0 ? input.getPageSize() : DEFAULT_PAGE_SIZE;
        final Pageable pageable = PageRequest.of(input.getPageNumber(), pageSize, sort);

        final Specification<Watchlist> specification = watchlistMetaData.toUserWatchlistSpecification(input);
        return watchlistRepository.findAll(specification, pageable);
    }

    /**
     * Gets a personal watchlist by ID for the logged-in user.
     *
     * @param watchlistId the watchlist ID
     * @return the watchlist
     * @throws DataNotFoundException if watchlist not found or not owned by user
     */
    @Transactional(readOnly = true)
    public Watchlist getWatchlist(final Long watchlistId) {
        final User user = getLoggedInUser();
        return watchlistRepository.findByIdAndUserId(watchlistId, user.getId())
            .orElseThrow(() -> new DataNotFoundException("Watchlist not found"));
    }

    /**
     * Gets watchlist channels for a watchlist.
     *
     * @param watchlistId the watchlist ID
     * @return list of watchlist channels
     */
    @Transactional(readOnly = true)
    public List<WatchlistChannel> getWatchlistChannels(final Long watchlistId) {
        return watchlistChannelRepository.findByWatchlistId(watchlistId);
    }

    /**
     * Updates a personal watchlist for the logged-in user.
     *
     * @param watchlistId the watchlist ID
     * @param request     the update request
     * @return the updated watchlist
     * @throws DataNotFoundException           if watchlist not found or not owned by user
     * @throws DuplicateWatchlistNameException if new name already exists
     */
    @Transactional
    public Watchlist updateWatchlist(final Long watchlistId, final UpdateWatchlistRequest request) {
        final User user = getLoggedInUser();
        final Watchlist watchlist = getWatchlist(watchlistId);

        // Check for duplicate name (excluding current watchlist)
        final Optional<Watchlist> existing = watchlistRepository.findByUserIdAndName(
            user.getId(),
            request.getName()
        );
        if (existing.isPresent() && !existing.get().getId().equals(watchlistId)) {
            throw new DuplicateWatchlistNameException();
        }

        // Update fields
        watchlist.setName(request.getName());
        watchlist.setDescription(request.getDescription());
        if (request.getDefaultTimeRange() != null) {
            watchlist.setDefaultTimeRange(request.getDefaultTimeRange());
        }
        if (request.getDefaultOnlyCritical() != null) {
            watchlist.setDefaultOnlyCritical(request.getDefaultOnlyCritical());
        }
        if (request.getDefaultSortBySeverity() != null) {
            watchlist.setDefaultSortBySeverity(request.getDefaultSortBySeverity());
        }
        watchlist.setUpdatedAt(OffsetDateTime.now());

        // Update channels if provided
        if (request.getChannelIds() != null) {
            watchlistChannelRepository.deleteAllByWatchlistId(watchlistId);
            if (!request.getChannelIds().isEmpty()) {
                addChannelsToWatchlist(watchlist, request.getChannelIds(), user.getId());
            }
        }

        return watchlistRepository.save(watchlist);
    }

    /**
     * Deletes a personal watchlist (hard delete).
     *
     * @param watchlistId the watchlist ID
     * @throws DataNotFoundException if watchlist not found or not owned by user
     */
    @Transactional
    public void deleteWatchlist(final Long watchlistId) {
        final Watchlist watchlist = getWatchlist(watchlistId);
        watchlistChannelRepository.deleteAllByWatchlistId(watchlistId);
        watchlistRepository.delete(watchlist);
    }

    /**
     * Adds channels to a watchlist.
     *
     * @param watchlist  the watchlist
     * @param channelIds the channel IDs
     * @param userId     the user ID
     */
    private void addChannelsToWatchlist(
        final Watchlist watchlist,
        final List<Long> channelIds,
        final Long userId
    ) {
        final List<WatchlistChannel> watchlistChannels = new ArrayList<>();
        int position = 0;

        for (final Long channelId : channelIds) {
            final Channel channel = channelRepository.findByIdAndUserId(channelId, userId)
                .orElseThrow(() -> new DataNotFoundException(CHANNEL_NOT_FOUND));

            final WatchlistChannel watchlistChannel = new WatchlistChannel();
            watchlistChannel.setId(new WatchlistChannelId(watchlist.getId(), channel.getId()));
            watchlistChannel.setWatchlist(watchlist);
            watchlistChannel.setChannel(channel);
            watchlistChannel.setPosition(position++);
            watchlistChannels.add(watchlistChannel);
        }

        watchlistChannelRepository.saveAll(watchlistChannels);
    }
}
