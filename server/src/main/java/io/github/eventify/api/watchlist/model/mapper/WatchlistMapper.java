package io.github.eventify.api.watchlist.model.mapper;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.mapper.ChannelGroupMapper;
import io.github.eventify.api.monitor.model.TimeRange;
import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import io.github.eventify.api.watchlist.model.WatchlistFilters;
import io.github.eventify.api.watchlist.model.request.CreateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.UpdateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.WatchlistConfigurationRequest;
import io.github.eventify.api.watchlist.model.request.WatchlistFiltersRequest;
import io.github.eventify.api.watchlist.model.response.WatchlistConfigurationResponse;
import io.github.eventify.api.watchlist.model.response.WatchlistDetailsResponse;
import io.github.eventify.api.watchlist.model.response.WatchlistFiltersResponse;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for watchlist entities and DTOs.
 *
 * <p>Uses {@link ChannelGroupMapper} for ChannelGroup conversions to avoid duplication.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = {
        DateTimeMapper.class,
        ChannelGroupMapper.class
    }
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
    @Mapping(
        target = "configuration",
        qualifiedByName = "configurationToResponse"
    )
    public abstract WatchlistDetailsResponse toResourceObject(Watchlist watchlist);

    /**
     * Maps WatchlistFilters domain to response DTO.
     * Converts TimeRange enum to its string value.
     *
     * @param filters the domain filters
     * @return the response filters
     */
    @Named("filtersToResponse")
    @Mapping(
        target = "timeRange",
        source = "timeRange",
        qualifiedByName = "timeRangeToString"
    )
    public abstract WatchlistFiltersResponse filtersToResponse(WatchlistFilters filters);

    /**
     * Maps WatchlistConfiguration domain to response DTO.
     * Uses ChannelGroupMapper for group conversion via 'uses' attribute.
     *
     * @param configuration the domain configuration
     * @return the response configuration
     */
    @Named("configurationToResponse")
    @Mapping(
        target = "groups",
        source = "groups",
        qualifiedByName = "toSimpleResponseList"
    )
    public abstract WatchlistConfigurationResponse configurationToResponse(WatchlistConfiguration configuration);

    /**
     * Converts TimeRange enum to its string value.
     *
     * @param timeRange the time range enum
     * @return the string value (e.g., "24h", "7d")
     */
    @Named("timeRangeToString")
    protected String timeRangeToString(final TimeRange timeRange) {
        return Objects.requireNonNullElse(timeRange, TimeRange.LAST_24H).getValue();
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
     * Uses ChannelGroupMapper for group conversion via 'uses' attribute.
     *
     * @param request the configuration request
     * @return the configuration domain object
     */
    @Mapping(
        target = "channels",
        source = "channelIds",
        qualifiedByName = "channelIdsToChannels"
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
            .map(Channel::new)
            .toList();
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
