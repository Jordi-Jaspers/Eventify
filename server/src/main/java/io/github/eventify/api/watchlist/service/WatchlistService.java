package io.github.eventify.api.watchlist.service;

import io.github.eventify.api.monitor.model.TimeRange;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistChannel;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import io.github.eventify.api.watchlist.model.WatchlistFilters;
import io.github.eventify.api.watchlist.model.WatchlistMetaData;
import io.github.eventify.api.watchlist.repository.WatchlistChannelRepository;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.eventify.common.exception.ApiErrorCode;
import io.github.eventify.common.util.TimeProvider;
import io.github.jframe.datasource.search.model.input.SortableColumn;
import io.github.jframe.exception.core.DataNotFoundException;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Abstract base class for watchlist services providing common functionality.
 */
public abstract class WatchlistService {

    protected static final int DEFAULT_PAGE_SIZE = 20;

    protected static final String WATCHLIST_NOT_FOUND = "Watchlist not found";

    protected final WatchlistRepository watchlistRepository;

    protected final WatchlistMetaData watchlistMetaData;

    protected final WatchlistChannelRepository watchlistChannelRepository;

    /**
     * Constructor.
     *
     * @param watchlistRepository        the watchlist repository
     * @param watchlistMetaData          the watchlist metadata
     * @param watchlistChannelRepository the watchlist channel junction repository
     */
    protected WatchlistService(
                               final WatchlistRepository watchlistRepository,
                               final WatchlistMetaData watchlistMetaData,
                               final WatchlistChannelRepository watchlistChannelRepository
    ) {
        this.watchlistRepository = watchlistRepository;
        this.watchlistMetaData = watchlistMetaData;
        this.watchlistChannelRepository = watchlistChannelRepository;
    }

    /**
     * Initializes default configuration if not provided.
     *
     * @param watchlist the watchlist to initialize
     */
    protected void initializeDefaults(final Watchlist watchlist) {
        if (watchlist.getConfiguration() == null) {
            watchlist.setConfiguration(WatchlistConfiguration.empty());
        }

        if (watchlist.getFilters() == null) {
            watchlist.setFilters(WatchlistFilters.defaults());
        } else {
            applyDefaultTimeRangeIfMissing(watchlist);
        }
    }

    /**
     * Applies default time range if not provided.
     *
     * @param watchlist the watchlist to update
     */
    protected void applyDefaultTimeRangeIfMissing(final Watchlist watchlist) {
        if (watchlist.getFilters() != null && watchlist.getFilters().getTimeRange() == null) {
            watchlist.getFilters().setTimeRange(TimeRange.LAST_24H);
        }
    }

    /**
     * Updates watchlist fields from the updated entity.
     *
     * @param existing the existing watchlist to update
     * @param updated  the watchlist with updated values
     */
    protected void applyUpdates(final Watchlist existing, final Watchlist updated) {
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setUpdatedAt(TimeProvider.now());

        if (updated.getConfiguration() != null) {
            existing.setConfiguration(updated.getConfiguration());
        }

        if (updated.getFilters() != null) {
            applyDefaultTimeRangeIfMissing(updated);
            existing.setFilters(updated.getFilters());
        }
    }

    /**
     * Syncs the watchlist_channel junction table for the given watchlist.
     * Deletes existing rows and inserts new ones based on the current configuration.
     *
     * @param watchlist the watchlist whose channels should be synced
     */
    protected void syncWatchlistChannels(final Watchlist watchlist) {
        if (watchlistChannelRepository != null) {
            watchlistChannelRepository.deleteByWatchlistId(watchlist.getId());

            final WatchlistConfiguration configuration = watchlist.getConfiguration();
            if (configuration != null) {
                final List<Long> standaloneIds = configuration.getChannelIds() != null
                    ? configuration.getChannelIds()
                    : List.of();

                final List<Long> groupIds = configuration.getGroups() != null
                    ? configuration.getGroups().stream()
                        .flatMap(group -> group.getChannelIds() != null ? group.getChannelIds().stream() : Stream.empty())
                        .toList()
                    : List.of();

                final List<Long> allChannelIds = Stream.concat(standaloneIds.stream(), groupIds.stream())
                    .distinct()
                    .toList();

                final List<WatchlistChannel> rows = allChannelIds.stream()
                    .map(channelId -> new WatchlistChannel(watchlist.getId(), channelId))
                    .toList();

                watchlistChannelRepository.saveAll(rows);
            }
        }
    }

    /**
     * Creates a pageable from input parameters.
     *
     * @param pageNumber the page number
     * @param pageSize   the page size
     * @param sortOrder  the sort order columns
     * @return the pageable
     */
    protected Pageable createPageable(
        final int pageNumber,
        final int pageSize,
        final List<SortableColumn> sortOrder
    ) {
        final Sort sort = watchlistMetaData.toSort(sortOrder);
        final int effectivePageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
        return PageRequest.of(pageNumber, effectivePageSize, sort);
    }

    /**
     * Validates that all requested channel IDs are present in the found channels list.
     * Throws {@link DataNotFoundException} for the first missing ID.
     *
     * @param requestedIds the channel IDs requested
     * @param foundIds     the channel IDs that were actually found
     */
    protected void validateFoundChannelIds(final List<Long> requestedIds, final Set<Long> foundIds) {
        requestedIds.stream()
            .filter(id -> !foundIds.contains(id))
            .findFirst()
            .ifPresent(id -> {
                throw new DataNotFoundException(ApiErrorCode.CHANNEL_NOT_FOUND);
            });
    }
}
