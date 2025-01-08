package org.jordijaspers.eventify.api.dashboard.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.jordijaspers.eventify.api.check.model.response.CheckResponse;

/**
 * Response object for a dashboard configuration.
 */
@Data
@NoArgsConstructor
public class DashboardConfigurationResponse {

    private final List<DashboardGroupResponse> groups = new ArrayList<>();

    private final List<CheckResponse> ungroupedChecks = new ArrayList<>();

    /**
     * A default constructor to create an existing dashboard configuration.
     *
     * @param groups          The groups of checks.
     * @param ungroupedChecks The ungrouped checks.
     */
    public DashboardConfigurationResponse(final List<DashboardGroupResponse> groups, final List<CheckResponse> ungroupedChecks) {
        this.groups.addAll(groups);
        this.ungroupedChecks.addAll(ungroupedChecks);
    }
}
