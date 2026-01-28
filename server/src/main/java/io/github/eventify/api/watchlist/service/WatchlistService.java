package io.github.eventify.api.watchlist.service;

import io.github.eventify.api.monitor.model.TimeRange;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import io.github.eventify.api.watchlist.model.WatchlistFilters;
import io.github.eventify.api.watchlist.model.WatchlistMetaData;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.jframe.datasource.search.model.input.SortableColumn;

import java.time.OffsetDateTime;
import java.util.List;

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

    /**
     * Constructor.
     *
     * @param watchlistRepository the watchlist repository
     * @param watchlistMetaData   the watchlist metadata
     */
    protected WatchlistService(final WatchlistRepository watchlistRepository, final WatchlistMetaData watchlistMetaData) {
        this.watchlistRepository = watchlistRepository;
        this.watchlistMetaData = watchlistMetaData;
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
        existing.setUpdatedAt(OffsetDateTime.now());

        if (updated.getConfiguration() != null) {
            existing.setConfiguration(updated.getConfiguration());
        }

        if (updated.getFilters() != null) {
            applyDefaultTimeRangeIfMissing(updated);
            existing.setFilters(updated.getFilters());
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
}
