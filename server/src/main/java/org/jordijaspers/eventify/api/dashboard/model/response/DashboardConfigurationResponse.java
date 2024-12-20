package org.jordijaspers.eventify.api.dashboard.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jordijaspers.eventify.api.check.model.response.CheckResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Response object for a dashboard configuration.
 */
@Data
@NoArgsConstructor
public class DashboardConfigurationResponse {

    private final List<DashboardGroupResponse> groups = new ArrayList<>();

    private final Set<CheckResponse> ungroupedChecks = new HashSet<>();

    /**
     * A default constructor to create an existing dashboard configuration.
     *
     * @param groups          The groups of checks.
     * @param ungroupedChecks The ungrouped checks.
     */
    public DashboardConfigurationResponse(final List<DashboardGroupResponse> groups, final Set<CheckResponse> ungroupedChecks) {
        this.groups.addAll(groups);
        this.ungroupedChecks.addAll(ungroupedChecks);
    }
}
