package io.github.eventify.api.dashboard.model.mapper;

import io.github.eventify.api.dashboard.model.DashboardStats;
import io.github.eventify.api.dashboard.model.response.DashboardStatsResponse;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting DashboardStats domain record to DashboardStatsResponse DTO.
 */
@Mapper(config = SharedMapperConfig.class)
public abstract class DashboardStatsMapper {

    public abstract DashboardStatsResponse toResponse(DashboardStats stats);
}
