package org.jordijaspers.eventify.api.dashboard.model.request;

import lombok.Data;

/**
 * Request object to update the details of a dashboard.
 */
@Data
public class UpdateDashboardDetailsRequest {

    private String name;

    private String description;

    private boolean global;

}
