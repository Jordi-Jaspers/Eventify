package org.jordijaspers.eventify.api.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.hawaiiframework.repository.DataNotFoundException;
import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.model.DashboardGroup;
import org.jordijaspers.eventify.api.dashboard.model.request.DashboardConfigurationRequest;
import org.jordijaspers.eventify.api.dashboard.model.request.GroupRequest;
import org.jordijaspers.eventify.api.dashboard.repository.DashboardRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import jakarta.transaction.Transactional;

import static org.jordijaspers.eventify.common.exception.ApiErrorCode.DASHBOARD_NOT_FOUND_ERROR;

@Service
@Transactional
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    /**
     * Retrieves the dashboard configuration.
     *
     * @param dashboardId The dashboard id.
     * @returnn The dashboard configuration.
     */
    public Dashboard getDashboardConfiguration(final Long dashboardId) {
        return dashboardRepository.findById(dashboardId)
            .orElseThrow(() -> new DataNotFoundException(DASHBOARD_NOT_FOUND_ERROR));
    }

    /**
     * Configures the dashboard with the given checks and dashboard groups.
     *
     * @param dashboardId The dashboard that needs to be configured.
     * @param request     The configuration request.
     * @return The configured dashboard.
     */
    public Dashboard configureDashboard(final Long dashboardId, final DashboardConfigurationRequest request) {
        final Dashboard dashboard = getDashboardConfiguration(dashboardId);
        dashboard.clearConfiguration();
        configureGroupedChecks(request.getGroups(), dashboard);
        configureUngroupedChecks(request, dashboard);
        return dashboardRepository.save(dashboard);
    }

    private static void configureUngroupedChecks(final DashboardConfigurationRequest request, final Dashboard dashboard) {
        int displayOrder = 1000;
        for (Long checkId : request.getUngroupedCheckIds()) {
            dashboard.addCheck(new Check(checkId), null, displayOrder++);
        }
    }

    private static void configureGroupedChecks(final List<GroupRequest> requests, final Dashboard dashboard) {
        requests.forEach(request -> {
            final DashboardGroup group = new DashboardGroup(dashboard, request.getName());
            dashboard.getGroups().add(group);

            int displayOrder = 1;
            for (final Long checkId : request.getCheckIds()) {
                dashboard.addCheck(new Check(checkId), group, displayOrder++);
            }
        });
    }
}
