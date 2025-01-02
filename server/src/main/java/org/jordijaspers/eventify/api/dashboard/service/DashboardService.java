package org.jordijaspers.eventify.api.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.hawaiiframework.repository.DataNotFoundException;
import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.model.DashboardGroup;
import org.jordijaspers.eventify.api.dashboard.model.request.CreateDashboardRequest;
import org.jordijaspers.eventify.api.dashboard.model.request.DashboardConfigurationRequest;
import org.jordijaspers.eventify.api.dashboard.model.request.DashboardGroupRequest;
import org.jordijaspers.eventify.api.dashboard.model.request.UpdateDashboardDetailsRequest;
import org.jordijaspers.eventify.api.dashboard.repository.DashboardCheckRepository;
import org.jordijaspers.eventify.api.dashboard.repository.DashboardGroupRepository;
import org.jordijaspers.eventify.api.dashboard.repository.DashboardRepository;
import org.jordijaspers.eventify.api.team.model.Team;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.exception.InvalidAccessException;
import org.jordijaspers.eventify.common.exception.UserNotPartOfTeamException;
import org.jordijaspers.eventify.common.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.transaction.Transactional;

import static org.jordijaspers.eventify.common.exception.ApiErrorCode.CANNOT_ACCESS_DASHBOARD;
import static org.jordijaspers.eventify.common.exception.ApiErrorCode.DASHBOARD_NOT_FOUND_ERROR;

@Service
@Transactional
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    private final DashboardGroupRepository dashboardGroupRepository;

    private final DashboardCheckRepository dashboardCheckRepository;

    /**
     * Retrieves the dashboard configuration.
     *
     * @param dashboardId The dashboard id.
     * @return The dashboard configuration.
     */
    public Dashboard getDashboardConfiguration(final Long dashboardId) {
        final User user = SecurityUtil.getLoggedInUser();
        final Dashboard dashboard = dashboardRepository.findByIdWithConfiguration(dashboardId)
            .orElseThrow(() -> new DataNotFoundException(DASHBOARD_NOT_FOUND_ERROR));

        if (!dashboard.isGlobal()) {
            user.getTeams().stream()
                .filter(team -> team.getId().equals(dashboard.getTeam().getId()))
                .findFirst()
                .orElseThrow(() -> new InvalidAccessException(CANNOT_ACCESS_DASHBOARD));
        }

        return dashboard;
    }

    /**
     * Retrieves all permitted dashboards for the current user by checking the teams the user is part of, unless the dashboard is marked as
     * global.
     *
     * @return The dashboards for the current user.
     */
    public List<Dashboard> getPermittedDashboards() {
        final User user = SecurityUtil.getLoggedInUser();
        return dashboardRepository.findAllPermittedDashboards(user.getTeams());
    }

    /**
     * Deletes the dashboard with the given id.
     *
     * @param id The id of the dashboard.
     */
    public void deleteDashboard(final Long id) {
        final Dashboard dashboard = getDashboardConfiguration(id);
        dashboardRepository.deleteById(dashboard.getId());
    }

    /**
     * Creates a new dashboard without any configuration.
     *
     * @param request The request to create the dashboard.
     * @return The created dashboard.
     */
    public Dashboard createDashboard(final CreateDashboardRequest request) {
        final User user = SecurityUtil.getLoggedInUser();
        final Team selectedTeam = user.getTeams().stream()
            .filter(team -> team.getId().equals(request.getTeamId()))
            .findFirst()
            .orElseThrow(UserNotPartOfTeamException::new);
        return save(new Dashboard(request, user, selectedTeam));
    }

    /**
     * Updates the details of the dashboard.
     *
     * @param id      The id of the dashboard.
     * @param request The request with the updated details.
     * @return The updated dashboard.
     */
    public Dashboard updateDashboard(final Long id, final UpdateDashboardDetailsRequest request) {
        final Dashboard dashboard = getDashboardConfiguration(id);
        dashboard.setName(request.getName());
        dashboard.setDescription(request.getDescription());
        dashboard.setGlobal(request.isGlobal());
        return save(dashboard);
    }

    /**
     * Configures the dashboard with the given checks and dashboard groups.
     *
     * @param dashboardId The dashboard that needs to be configured.
     * @param request     The configuration request.
     * @return The configured dashboard.
     */
    public Dashboard configureDashboard(final Long dashboardId, final DashboardConfigurationRequest request) {
        dashboardCheckRepository.deleteConfigurationForDashboard(dashboardId);
        dashboardGroupRepository.deleteGroupsForDashboard(dashboardId);

        final Dashboard dashboard = getDashboardConfiguration(dashboardId);
        configureGroupedChecks(request.getGroups(), dashboard);
        configureUngroupedChecks(request, dashboard);
        return save(dashboard);
    }

    private Dashboard save(final Dashboard dashboard) {
        dashboard.setUpdatedBy(SecurityUtil.getLoggedInUser().getUsername());
        dashboard.setLastUpdated(LocalDateTime.now());
        return dashboardRepository.save(dashboard);
    }

    private static void configureUngroupedChecks(final DashboardConfigurationRequest request, final Dashboard dashboard) {
        int displayOrder = 1000;
        for (Long checkId : request.getUngroupedCheckIds()) {
            dashboard.addCheck(new Check(checkId), null, displayOrder++);
        }
    }

    private static void configureGroupedChecks(final List<DashboardGroupRequest> requests, final Dashboard dashboard) {
        int groupOrder = 1;
        for (final DashboardGroupRequest request : requests) {
            final DashboardGroup group = new DashboardGroup(dashboard, request.getName(), groupOrder++);
            dashboard.getGroups().add(group);

            int checkOrder = 1;
            for (final Long checkId : request.getCheckIds()) {
                dashboard.addCheck(new Check(checkId), group, checkOrder++);
            }
        }
    }
}
