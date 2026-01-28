package io.github.eventify.api.watchlist.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistMetaData;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.eventify.common.exception.DuplicateWatchlistNameException;
import io.github.jframe.datasource.search.model.input.SearchInput;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.exception.core.DataNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.api.watchlist.model.WatchlistMetaData.USER_TERM;
import static io.github.eventify.common.exception.ApiErrorCode.CHANNEL_NOT_FOUND;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;

/**
 * Service for managing user (personal) watchlists.
 */
@Service
public class UserWatchlistService extends WatchlistService {

    private final ChannelRepository channelRepository;

    /**
     * Constructor.
     *
     * @param watchlistRepository the watchlist repository
     * @param channelRepository   the channel repository
     * @param watchlistMetaData   the watchlist metadata
     */
    public UserWatchlistService(
                                final WatchlistRepository watchlistRepository,
                                final ChannelRepository channelRepository,
                                final WatchlistMetaData watchlistMetaData
    ) {
        super(watchlistRepository, watchlistMetaData);
        this.channelRepository = channelRepository;
    }

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

        if (watchlist.getConfiguration() != null) {
            validateChannelIds(watchlist.getConfiguration().getChannelIds(), user.getId());
        }
        initializeDefaults(watchlist);

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

        final Pageable pageable = createPageable(input.getPageNumber(), input.getPageSize(), input.getSortOrder());
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
            .orElseThrow(() -> new DataNotFoundException(WATCHLIST_NOT_FOUND));
    }

    /**
     * Updates a personal watchlist for the logged-in user.
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

        if (updated.getConfiguration() != null) {
            validateChannelIds(updated.getConfiguration().getChannelIds(), user.getId());
        }
        applyUpdates(watchlist, updated);

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
     * Validates that all channel IDs exist and belong to the user.
     *
     * @param channelIds the channel IDs to validate
     * @param userId     the user ID
     */
    private void validateChannelIds(final List<Long> channelIds, final Long userId) {
        if (channelIds == null || channelIds.isEmpty()) {
            return;
        }

        final Set<Long> foundIds = channelRepository.findAllByIdInAndUserId(channelIds, userId)
            .stream()
            .map(Channel::getId)
            .collect(Collectors.toSet());

        channelIds.stream()
            .filter(id -> !foundIds.contains(id))
            .findFirst()
            .ifPresent(id -> {
                throw new DataNotFoundException(CHANNEL_NOT_FOUND);
            });
    }
}
