package org.jordijaspers.eventify.common.security.filter;

import lombok.RequiredArgsConstructor;

import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.service.DashboardService;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.common.exception.AuthorizationException;
import org.springframework.stereotype.Service;

import static org.jordijaspers.eventify.common.exception.ApiErrorCode.CANNOT_ACCESS_DASHBOARD;
import static org.jordijaspers.eventify.common.util.SecurityUtil.getLoggedInUser;

/**
 * The DashboardSecurityService provides security checks for the dashboard.
 */
@Service
@RequiredArgsConstructor
public class DashboardSecurityService {

    private final DashboardService dashboardService;

    /**
     * Check if the currently authenticated user has access to the dashboard.
     *
     * @param dashboardId The dashboard id.
     * @return True if the user has access.
     */
    public boolean hasDashboardAccess(final Long dashboardId) {
        final User user = getLoggedInUser();
        final Dashboard dashboard = dashboardService.getDashboardConfiguration(dashboardId);

        final boolean hasAccess = dashboard.isGlobal() || user.getTeams().stream()
            .anyMatch(team -> team.getId().equals(dashboard.getTeam().getId()));

        if (!hasAccess) {
            throw new AuthorizationException(CANNOT_ACCESS_DASHBOARD);
        }

        return true;
    }
}
