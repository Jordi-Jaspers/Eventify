package io.github.eventify.api.watchlist.service;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.repository.ChannelRepository;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.service.OrganizationService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistMetaData;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import io.github.eventify.common.exception.DuplicateWatchlistNameException;
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

import static io.github.eventify.common.exception.ApiErrorCode.CHANNEL_NOT_FOUND;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;

/**
 * Service for managing organization watchlists.
 */
@Service
public class OrganizationWatchlistService extends WatchlistService {

    private final ChannelRepository channelRepository;

    private final OrganizationService organizationService;

    /**
     * Constructor.
     *
     * @param watchlistRepository the watchlist repository
     * @param channelRepository   the channel repository
     * @param watchlistMetaData   the watchlist metadata
     * @param organizationService the organization service
     */
    public OrganizationWatchlistService(
                                        final WatchlistRepository watchlistRepository,
                                        final ChannelRepository channelRepository,
                                        final WatchlistMetaData watchlistMetaData,
                                        final OrganizationService organizationService
    ) {
        super(watchlistRepository, watchlistMetaData);
        this.channelRepository = channelRepository;
        this.organizationService = organizationService;
    }

    /**
     * Creates a new organization watchlist.
     *
     * @param organizationId the organization ID
     * @param watchlist      the watchlist to create (mapped from request)
     * @return the created watchlist
     */
    @Transactional
    public Watchlist createWatchlist(final Long organizationId, final Watchlist watchlist) {
        final Organization organization = organizationService.getOrganization(organizationId);

        final Optional<Watchlist> existing = watchlistRepository.findByOrganizationIdAndName(
            organizationId,
            watchlist.getName()
        );
        if (existing.isPresent()) {
            throw new DuplicateWatchlistNameException();
        }

        final User user = getLoggedInUser();
        watchlist.setUser(user);
        watchlist.setOrganization(organization);

        if (watchlist.getConfiguration() != null) {
            validateChannelIds(watchlist.getConfiguration().getChannelIds(), organizationId);
        }
        initializeDefaults(watchlist);

        return watchlistRepository.save(watchlist);
    }

    /**
     * Searches organization watchlists with pagination, filtering, and sorting.
     *
     * @param organizationId the organization ID
     * @param input          the sortable page input containing search parameters
     * @return page of watchlists matching the search criteria
     */
    @Transactional(readOnly = true)
    public Page<Watchlist> searchWatchlists(final Long organizationId, final SortablePageInput input) {
        organizationService.getOrganization(organizationId);

        final Pageable pageable = createPageable(input.getPageNumber(), input.getPageSize(), input.getSortOrder());
        final Specification<Watchlist> specification = watchlistMetaData.toOrganizationWatchlistSpecification(
            input,
            organizationId
        );
        return watchlistRepository.findAll(specification, pageable);
    }

    /**
     * Gets an organization watchlist by ID.
     *
     * @param organizationId the organization ID
     * @param watchlistId    the watchlist ID
     * @return the watchlist
     * @throws DataNotFoundException if watchlist not found or not in organization
     */
    @Transactional(readOnly = true)
    public Watchlist getWatchlist(final Long organizationId, final Long watchlistId) {
        organizationService.getOrganization(organizationId);

        return watchlistRepository.findByIdAndOrganizationId(watchlistId, organizationId)
            .orElseThrow(() -> new DataNotFoundException(WATCHLIST_NOT_FOUND));
    }

    /**
     * Updates an organization watchlist.
     *
     * @param organizationId the organization ID
     * @param watchlistId    the watchlist ID
     * @param updated        the watchlist with updated values (from mapper)
     * @return the updated watchlist
     * @throws DataNotFoundException           if watchlist not found or not in organization
     * @throws DuplicateWatchlistNameException if new name already exists
     */
    @Transactional
    public Watchlist updateWatchlist(final Long organizationId, final Long watchlistId, final Watchlist updated) {
        final Watchlist watchlist = getWatchlist(organizationId, watchlistId);

        final Optional<Watchlist> existing = watchlistRepository.findByOrganizationIdAndName(
            organizationId,
            updated.getName()
        );
        if (existing.isPresent() && !existing.get().getId().equals(watchlistId)) {
            throw new DuplicateWatchlistNameException();
        }

        if (updated.getConfiguration() != null) {
            validateChannelIds(updated.getConfiguration().getChannelIds(), organizationId);
        }
        applyUpdates(watchlist, updated);

        return watchlistRepository.save(watchlist);
    }

    /**
     * Deletes an organization watchlist (hard delete).
     *
     * @param organizationId the organization ID
     * @param watchlistId    the watchlist ID
     * @throws DataNotFoundException if watchlist not found or not in organization
     */
    @Transactional
    public void deleteWatchlist(final Long organizationId, final Long watchlistId) {
        final Watchlist watchlist = getWatchlist(organizationId, watchlistId);
        watchlistRepository.delete(watchlist);
    }

    /**
     * Validates that all channel IDs exist and belong to the organization.
     *
     * @param channelIds     the channel IDs to validate
     * @param organizationId the organization ID
     */
    private void validateChannelIds(final List<Long> channelIds, final Long organizationId) {
        if (channelIds == null || channelIds.isEmpty()) {
            return;
        }

        final Set<Long> foundIds = channelRepository.findAllByIdInAndOrganizationId(channelIds, organizationId)
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
