package org.jordijaspers.eventify.api.dashboard.model.response;

import lombok.Data;
import org.jordijaspers.eventify.api.team.model.response.TeamResponse;

import java.time.ZonedDateTime;

/**
 * A response model for a dashboard.
 */
@Data
public class DashboardResponse {

    private Long id;

    private String name;

    private String description;

    private boolean global;

    private ZonedDateTime created;

    private ZonedDateTime lastUpdated;

    private String updatedBy;

    private TeamResponse team;

    private DashboardConfigurationResponse configuration = new DashboardConfigurationResponse();

}
