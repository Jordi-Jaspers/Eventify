package org.jordijaspers.eventify.api.dashboard.model.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class DashboardConfigurationRequest {

    private final List<GroupRequest> groups = new ArrayList<>();

    private final Set<Long> ungroupedCheckIds = new HashSet<>();

}
