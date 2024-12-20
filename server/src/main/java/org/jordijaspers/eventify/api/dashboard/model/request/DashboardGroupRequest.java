package org.jordijaspers.eventify.api.dashboard.model.request;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Request object to create a new dashboard group.
 */
@Data
public class DashboardGroupRequest {

    private final String name;

    private final String description;

    private final Set<Long> checkIds = new HashSet<>();

}
