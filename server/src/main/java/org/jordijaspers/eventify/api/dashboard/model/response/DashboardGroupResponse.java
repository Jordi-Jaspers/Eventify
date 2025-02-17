package org.jordijaspers.eventify.api.dashboard.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.jordijaspers.eventify.api.check.model.response.CheckResponse;

/**
 * Response object for a dashboard group.
 */
@Data
@NoArgsConstructor
public class DashboardGroupResponse {

    private String name;

    private List<CheckResponse> checks = new ArrayList<>();

    /**
     * A default constructor to create an existing dashboard group.
     *
     * @param name   The name of the group.
     * @param checks The checks in the group.
     */
    public DashboardGroupResponse(final String name, final List<CheckResponse> checks) {
        this.name = name;
        this.checks.addAll(checks);
    }
}
