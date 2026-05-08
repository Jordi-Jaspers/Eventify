package io.github.eventify.api.monitor.model.mapper;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.mapper.ChannelGroupMapper;
import io.github.eventify.api.channel.model.response.ChannelResponse;
import io.github.eventify.api.monitor.model.MonitorResult;
import io.github.eventify.api.monitor.model.response.DashboardResponse;
import io.github.eventify.api.monitor.model.response.MonitorResponse;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for monitor domain objects to response DTOs.
 *
 * <p>Uses {@link ChannelGroupMapper} for ChannelGroup conversions to avoid duplication.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = ChannelGroupMapper.class
)
public abstract class MonitorMapper {

    /**
     * Maps a Channel (with transient timeline fields) to ChannelResponse.
     *
     * @param channel the channel
     * @return channel response
     */
    @Mapping(
        source = "id",
        target = "channelId"
    )
    @Mapping(
        source = "name",
        target = "channelName"
    )
    public abstract ChannelResponse toChannelResponse(Channel channel);

    /**
     * Maps a list of Channels to ChannelResponses.
     *
     * @param channels the channels
     * @return channel responses
     */
    public abstract List<ChannelResponse> toChannelResponses(List<Channel> channels);

    /**
     * Maps a WatchlistConfiguration to DashboardResponse.
     * Uses ChannelGroupMapper for group conversion via 'uses' attribute.
     *
     * @param configuration the watchlist configuration
     * @return dashboard response
     */
    public abstract DashboardResponse toDashboardResponse(WatchlistConfiguration configuration);

    /**
     * Maps MonitorResult to MonitorResponse.
     *
     * @param result the monitor result
     * @return monitor response
     */
    @Mapping(
        source = "watchlist.id",
        target = "watchlistId"
    )
    @Mapping(
        source = "watchlist.name",
        target = "watchlistName"
    )
    @Mapping(
        source = "timeRange.start",
        target = "rangeStart"
    )
    @Mapping(
        source = "timeRange.end",
        target = "rangeEnd"
    )
    @Mapping(
        source = "timeRange.live",
        target = "live"
    )
    @Mapping(
        source = "configuration",
        target = "dashboard"
    )
    public abstract MonitorResponse toResponse(MonitorResult result);
}
