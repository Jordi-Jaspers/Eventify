package org.jordijaspers.eventify.api.dashboard.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object to update the details of a dashboard.
 */
@Data
@NoArgsConstructor
public class UpdateDashboardDetailsRequest {

    private String name;

    private String description;

    private boolean global;

}
