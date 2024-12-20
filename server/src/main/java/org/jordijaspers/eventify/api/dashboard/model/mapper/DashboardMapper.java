package org.jordijaspers.eventify.api.dashboard.model.mapper;

import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.model.response.DashboardResponse;
import org.jordijaspers.eventify.api.team.model.mapper.TeamMapper;
import org.jordijaspers.eventify.common.mapper.DateTimeMapper;
import org.jordijaspers.eventify.common.mapper.config.SharedMapperConfig;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * The mapper for the dashboards.
 */
@Mapper(
    config = SharedMapperConfig.class,
    uses = {
        DateTimeMapper.class,
        TeamMapper.class,
        DashboardConfigurationMapper.class
    }
)
public abstract class DashboardMapper {

    @Mapping(
        target = "team",
        qualifiedByName = "toTeamResponseWithoutMembers"
    )
    @Mapping(
        target = "configuration",
        source = "dashboard",
        qualifiedByName = "toDashboardConfiguration"
    )
    @Named("toDashboardResponse")
    public abstract DashboardResponse toDashboardResponse(Dashboard dashboard);

    @Mapping(
        target = "team",
        qualifiedByName = "toTeamResponseWithoutMembers"
    )
    @Mapping(
        target = "configuration",
        ignore = true
    )
    @Named("toDashboardResponseWithoutConfig")
    public abstract DashboardResponse toDashboardResponseWithoutConfig(Dashboard dashboard);

    @IterableMapping(qualifiedByName = "toDashboardResponseWithoutConfig")
    public abstract List<DashboardResponse> toDashboardResponses(List<Dashboard> dashboards);


}
