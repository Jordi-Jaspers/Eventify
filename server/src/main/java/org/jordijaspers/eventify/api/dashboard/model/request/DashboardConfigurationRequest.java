package org.jordijaspers.eventify.api.dashboard.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Request object for configuring a dashboard.
 */
@Data
@NoArgsConstructor
public class DashboardConfigurationRequest {

    private List<DashboardGroupRequest> groups = new ArrayList<>();

    private Set<Long> ungroupedCheckIds = new HashSet<>();

}
