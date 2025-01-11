package org.jordijaspers.eventify.api.dashboard.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Request object to update the details of a dashboard.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateDashboardDetailsRequest {

    private String name;

    private String description;

    private boolean global;

}
