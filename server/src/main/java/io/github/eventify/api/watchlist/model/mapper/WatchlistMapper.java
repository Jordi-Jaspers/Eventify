package io.github.eventify.api.watchlist.model.mapper;

import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistChannel;
import io.github.eventify.api.watchlist.model.response.WatchlistChannelResponse;
import io.github.eventify.api.watchlist.model.response.WatchlistDetailsResponse;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper for watchlist entities to DTOs.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = DateTimeMapper.class
)
public abstract class WatchlistMapper extends PageMapper<WatchlistDetailsResponse, Watchlist> {

    /**
     * Maps Watchlist entity to WatchlistDetailsResponse.
     *
     * @param watchlist the watchlist entity
     * @return the response DTO
     */
    @Override
    @Named("toResourceObject")
    public WatchlistDetailsResponse toResourceObject(final Watchlist watchlist) {
        final WatchlistDetailsResponse response = toBasicResourceObject(watchlist);
        response.setChannels(List.of());
        return response;
    }

    /**
     * Maps Watchlist entity to WatchlistDetailsResponse with channels.
     *
     * @param watchlist the watchlist entity
     * @param channels  the watchlist channels
     * @return the response DTO
     */
    public WatchlistDetailsResponse toDetailsResponse(
        final Watchlist watchlist,
        final List<WatchlistChannel> channels
    ) {
        final WatchlistDetailsResponse response = toBasicResourceObject(watchlist);
        response.setChannels(
            channels.stream()
                .map(this::toChannelResponse)
                .collect(Collectors.toList())
        );
        return response;
    }

    /**
     * Maps Watchlist entity to WatchlistDetailsResponse (without channels).
     *
     * @param watchlist the watchlist entity
     * @return the response DTO
     */
    public WatchlistDetailsResponse toDetailsResponse(final Watchlist watchlist) {
        return toResourceObject(watchlist);
    }

    /**
     * Maps list of Watchlist entities to list of WatchlistDetailsResponse.
     *
     * @param watchlists the list of watchlist entities
     * @return the list of response DTOs
     */
    @IterableMapping(qualifiedByName = "toResourceObject")
    public abstract List<WatchlistDetailsResponse> toResourceObjects(List<Watchlist> watchlists);

    /**
     * Maps Watchlist basic fields to WatchlistDetailsResponse.
     *
     * @param watchlist the watchlist entity
     * @return the response DTO
     */
    protected abstract WatchlistDetailsResponse toBasicResourceObject(Watchlist watchlist);

    /**
     * Maps WatchlistChannel to WatchlistChannelResponse.
     *
     * @param watchlistChannel the watchlist channel entity
     * @return the channel response DTO
     */
    @Mapping(
        target = "id",
        source = "channel.id"
    )
    @Mapping(
        target = "name",
        source = "channel.name"
    )
    @Mapping(
        target = "status",
        expression = "java(watchlistChannel.getChannel().getStatus().name())"
    )
    @Mapping(
        target = "position",
        source = "position"
    )
    protected abstract WatchlistChannelResponse toChannelResponse(WatchlistChannel watchlistChannel);
}
