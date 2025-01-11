package org.jordijaspers.eventify.api.dashboard.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Request object to create a new dashboard.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class CreateDashboardRequest {

    private String name;

    private String description;

    private Long teamId;

    private boolean global;

}
