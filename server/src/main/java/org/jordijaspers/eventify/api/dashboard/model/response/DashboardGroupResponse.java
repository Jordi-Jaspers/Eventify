package org.jordijaspers.eventify.api.dashboard.model.response;

import lombok.Data;
import org.jordijaspers.eventify.api.check.model.response.CheckResponse;

import java.util.HashSet;
import java.util.Set;

/**
 * Response object for a dashboard group.
 */
@Data
public class DashboardGroupResponse {

    private final String name;

    private final Set<CheckResponse> checks = new HashSet<>();

    /**
     * A default constructor to create an existing dashboard group.
     *
     * @param name   The name of the group.
     * @param checks The checks in the group.
     */
    public DashboardGroupResponse(final String name, final Set<CheckResponse> checks) {
        this.name = name;
    }

}
