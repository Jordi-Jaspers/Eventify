package org.jordijaspers.eventify.api.dashboard.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object to create a new dashboard.
 */
@Data
@NoArgsConstructor
public class CreateDashboardRequest {

    private String name;

    private String description;

    private Long teamId;

    private boolean global;

}
