package org.jordijaspers.eventify.api.dashboard.model.request;

import lombok.Data;

/**
 * Request object to create a new dashboard.
 */
@Data
public class CreateDashboardRequest {

    private String name;

    private String description;

    private Long teamId;

    private boolean global;

}
