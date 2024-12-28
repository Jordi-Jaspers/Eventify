package org.jordijaspers.eventify.api.dashboard.model.mapper;

import lombok.RequiredArgsConstructor;
import org.jordijaspers.eventify.api.check.model.mapper.CheckMapper;
import org.jordijaspers.eventify.api.check.model.response.CheckResponse;
import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.model.DashboardCheck;
import org.jordijaspers.eventify.api.dashboard.model.DashboardGroup;
import org.jordijaspers.eventify.api.dashboard.model.response.DashboardConfigurationResponse;
import org.jordijaspers.eventify.api.dashboard.model.response.DashboardGroupResponse;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * The mapper for the dashboards.
 */
@Component
@RequiredArgsConstructor
public class DashboardConfigurationMapper {

    private final CheckMapper checkMapper;

    /**
     * Convert the groups and checks of a dashboard to a readable configuration object by dividing the checks into groups. If a check is not
     * in a group, it will be added to the ungrouped checks.
     *
     * @param dashboard The dashboard to convert.
     * @return The configuration object.
     */
    @Named("toDashboardConfiguration")
    public DashboardConfigurationResponse toDashboardConfiguration(Dashboard dashboard) {
        if (isNull(dashboard)) {
            return new DashboardConfigurationResponse();
        }

        final List<CheckResponse> ungroupedChecks = dashboard.getDashboardChecks().stream()
            .filter(dashboardCheck -> isNull(dashboardCheck.getGroup()))
            .sorted(Comparator.comparing(DashboardCheck::getDisplayOrder))
            .map(dashboardCheck -> checkMapper.toCheckResponse(dashboardCheck.getCheck()))
            .toList();

        final Map<DashboardGroup, List<CheckResponse>> groupedChecks = dashboard.getDashboardChecks().stream()
            .filter(dashboardCheck -> nonNull(dashboardCheck.getGroup()))
            .collect(
                Collectors.groupingBy(
                    DashboardCheck::getGroup,
                    Collectors.mapping(
                        dashboardCheck -> checkMapper.toCheckResponse(dashboardCheck.getCheck()),
                        Collectors.toList()
                    )
                )
            );

        final List<DashboardGroupResponse> groups = dashboard.getGroups().stream()
            .map(group -> new DashboardGroupResponse(group.getName(), groupedChecks.getOrDefault(group, List.of())))
            .toList();

        return new DashboardConfigurationResponse(groups, ungroupedChecks);
    }
}
