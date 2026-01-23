package io.github.eventify.api.watchlist.model.mapper;

import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistConfiguration;
import io.github.eventify.api.watchlist.model.WatchlistFilters;
import io.github.eventify.api.watchlist.model.request.CreateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.UpdateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.WatchlistConfigurationRequest;
import io.github.eventify.api.watchlist.model.request.WatchlistFiltersRequest;
import io.github.eventify.api.watchlist.model.response.WatchlistDetailsResponse;
import io.github.jframe.datasource.search.model.mapper.PageMapper;
import io.github.jframe.util.mapper.DateTimeMapper;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.Mapper;
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
    public abstract WatchlistDetailsResponse toResourceObject(Watchlist watchlist);

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
    public abstract WatchlistConfiguration toConfiguration(WatchlistConfigurationRequest request);

    /**
     * Maps WatchlistFiltersRequest to domain.
     * Used by MapStruct for nested mapping in toWatchlist methods.
     *
     * @param request the filters request
     * @return the filters domain object
     */
    public abstract WatchlistFilters toFilters(WatchlistFiltersRequest request);
}
