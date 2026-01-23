package io.github.eventify.api.watchlist.service;

import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import io.github.eventify.api.watchlist.model.WatchlistFilters;
import io.github.eventify.api.watchlist.model.WatchlistMetaData;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.eventify.common.exception.DuplicateWatchlistNameException;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
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
public class WatchlistService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String DEFAULT_TIME_RANGE = "24h";

    private final WatchlistRepository watchlistRepository;

    private final ChannelRepository channelRepository;

    private final WatchlistMetaData watchlistMetaData;

    /**
     * Creates a new personal watchlist for the logged-in user.
     *
     * @param watchlist the watchlist to create (mapped from request)
     * @return the created watchlist
     */
    @Transactional
    public Watchlist createWatchlist(final Watchlist watchlist) {
        final User user = getLoggedInUser();
        final Optional<Watchlist> existing = watchlistRepository.findByUserIdAndName(user.getId(), watchlist.getName());
        if (existing.isPresent()) {
            throw new DuplicateWatchlistNameException();
        }

        watchlist.setUser(user);
        if (watchlist.getConfiguration() == null) {
            watchlist.setConfiguration(WatchlistConfiguration.empty());
        } else {
            validateChannelIds(watchlist.getConfiguration().getChannelIds(), user.getId());
        }

        if (watchlist.getFilters() == null) {
            watchlist.setFilters(WatchlistFilters.defaults());
        } else {
            applyDefaultTimeRangeIfMissing(watchlist);
        }

        return watchlistRepository.save(watchlist);
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
     * Updates a personal watchlist for the logged-in user.
     * The watchlist entity should already be updated via mapper.
     *
     * @param watchlistId the watchlist ID
     * @param updated     the watchlist with updated values (from mapper)
     * @return the updated watchlist
     * @throws DataNotFoundException           if watchlist not found or not owned by user
     * @throws DuplicateWatchlistNameException if new name already exists
     */
    @Transactional
    public Watchlist updateWatchlist(final Long watchlistId, final Watchlist updated) {
        final User user = getLoggedInUser();
        final Watchlist watchlist = getWatchlist(watchlistId);
        final Optional<Watchlist> existing = watchlistRepository.findByUserIdAndName(user.getId(), updated.getName());
        if (existing.isPresent() && !existing.get().getId().equals(watchlistId)) {
            throw new DuplicateWatchlistNameException();
        }

        watchlist.setName(updated.getName());
        watchlist.setDescription(updated.getDescription());
        watchlist.setUpdatedAt(OffsetDateTime.now());

        if (updated.getConfiguration() != null) {
            validateChannelIds(updated.getConfiguration().getChannelIds(), user.getId());
            watchlist.setConfiguration(updated.getConfiguration());
        }

        if (updated.getFilters() != null) {
            applyDefaultTimeRangeIfMissing(updated);
            watchlist.setFilters(updated.getFilters());
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
        watchlistRepository.delete(watchlist);
    }

    /**
     * Removes a channel ID from all watchlists that contain it.
     * Called when a channel is deleted.
     *
     * @param channelId the channel ID to remove
     */
    @Transactional
    public void removeChannelFromAllWatchlists(final Long channelId) {
        watchlistRepository.removeChannelFromAllConfigurations(channelId);
    }

    /**
     * Validates that all channel IDs exist and belong to the user.
     */
    private void validateChannelIds(final List<Long> channelIds, final Long userId) {
        if (channelIds == null || channelIds.isEmpty()) {
            return;
        }
        for (final Long channelId : channelIds) {
            channelRepository.findByIdAndUserId(channelId, userId)
                .orElseThrow(() -> new DataNotFoundException(CHANNEL_NOT_FOUND));
        }
    }

    /**
     * Applies default time range if not provided.
     */
    private void applyDefaultTimeRangeIfMissing(final Watchlist watchlist) {
        if (watchlist.getFilters() != null && watchlist.getFilters().getTimeRange() == null) {
            watchlist.getFilters().setTimeRange(DEFAULT_TIME_RANGE);
        }
    }
}
