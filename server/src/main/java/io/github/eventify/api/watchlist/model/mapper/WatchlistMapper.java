package io.github.eventify.api.watchlist.model.mapper;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.monitor.model.TimeRange;
import io.github.eventify.api.watchlist.model.ChannelGroup;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import io.github.eventify.api.watchlist.model.WatchlistFilters;
import io.github.eventify.api.watchlist.model.request.ChannelGroupRequest;
import io.github.eventify.api.watchlist.model.request.CreateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.UpdateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.WatchlistConfigurationRequest;
import io.github.eventify.api.watchlist.model.request.WatchlistFiltersRequest;
import io.github.eventify.api.watchlist.model.response.WatchlistDetailsResponse;
import io.github.eventify.api.watchlist.model.response.WatchlistFiltersResponse;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for watchlist entities and DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class WatchlistMapper extends PageMapper<WatchlistDetailsResponse, Watchlist> {

    // ==================== Entity -> Response ====================

    /**
     * Maps Watchlist entity to WatchlistDetailsResponse.
     *
     * @param watchlist the watchlist entity
     * @return the response DTO
     */
    @Override
    @Named("toResourceObject")
    @Mapping(
        target = "filters",
        qualifiedByName = "filtersToResponse"
    )
    public abstract WatchlistDetailsResponse toResourceObject(Watchlist watchlist);

    /**
     * Maps WatchlistFilters domain to response DTO.
     * Uses @JsonValue for automatic TimeRange serialization.
     *
     * @param filters the domain filters
     * @return the response filters
     */
    @Named("filtersToResponse")
    public WatchlistFiltersResponse filtersToResponse(final WatchlistFilters filters) {
        if (filters == null) {
            return null;
        }
        final WatchlistFiltersResponse response = new WatchlistFiltersResponse();
        response.setTimeRange(
            filters.getTimeRange() != null
                ? filters.getTimeRange().getValue()
                : TimeRange.LAST_24H.getValue()
        );
        response.setOnlyCritical(filters.isOnlyCritical());
        response.setSortBySeverity(filters.isSortBySeverity());
        response.setGroupedView(filters.isGroupedView());
        return response;
    }

    // ==================== Request -> Entity ====================

    /**
     * Maps CreateWatchlistRequest to Watchlist entity.
     * Note: user and organization must be set separately.
     *
     * @param request the create request
     * @return the watchlist entity
     */
    public abstract Watchlist toWatchlist(CreateWatchlistRequest request);

    /**
     * Maps UpdateWatchlistRequest to Watchlist entity.
     * Note: user and organization must be set separately.
     *
     * @param request the update request
     * @return the watchlist entity with updated values
     */
    public abstract Watchlist toWatchlist(UpdateWatchlistRequest request);

    // ==================== Nested Request -> Domain ====================

    /**
     * Maps WatchlistConfigurationRequest to domain.
     * Used by MapStruct for nested mapping in toWatchlist methods.
     *
     * @param request the configuration request
     * @return the configuration domain object
     */
    @Mapping(
        target = "channels",
        source = "channelIds",
        qualifiedByName = "channelIdsToChannels"
    )
    @Mapping(
        target = "groups",
        source = "groups",
        qualifiedByName = "groupRequestsToGroups"
    )
    public abstract WatchlistConfiguration toConfiguration(WatchlistConfigurationRequest request);

    /**
     * Converts channel IDs to Channel objects with only the id field populated.
     *
     * @param channelIds the list of channel IDs
     * @return the list of Channel objects
     */
    @Named("channelIdsToChannels")
    protected List<Channel> channelIdsToChannels(final List<Long> channelIds) {
        if (channelIds == null) {
            return new ArrayList<>();
        }
        return channelIds.stream()
            .map(this::channelWithId)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Converts group requests to ChannelGroup objects.
     *
     * @param groupRequests the list of group requests
     * @return the list of ChannelGroup objects
     */
    @Named("groupRequestsToGroups")
    protected List<ChannelGroup> groupRequestsToGroups(final List<ChannelGroupRequest> groupRequests) {
        if (groupRequests == null) {
            return new ArrayList<>();
        }
        return groupRequests.stream()
            .map(this::toChannelGroup)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Converts a single group request to a ChannelGroup.
     *
     * @param request the group request
     * @return the ChannelGroup
     */
    private ChannelGroup toChannelGroup(final ChannelGroupRequest request) {
        return ChannelGroup.builder()
            .id(request.getId() != null ? request.getId() : UUID.randomUUID())
            .name(request.getName())
            .channels(channelIdsToChannels(request.getChannelIds()))
            .build();
    }

    /**
     * Creates a Channel with only the id populated.
     */
    private Channel channelWithId(final Long id) {
        final Channel channel = new Channel();
        channel.setId(id);
        return channel;
    }

    /**
     * Maps WatchlistFiltersRequest to domain.
     * Used by MapStruct for nested mapping in toWatchlist methods.
     *
     * @param request the filters request
     * @return the filters domain object
     */
    public abstract WatchlistFilters toFilters(WatchlistFiltersRequest request);
}
