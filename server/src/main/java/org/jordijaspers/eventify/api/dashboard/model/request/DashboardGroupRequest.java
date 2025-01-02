package org.jordijaspers.eventify.api.dashboard.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Request object to create a new dashboard group.
 */
@Data
@NoArgsConstructor
public class DashboardGroupRequest {

    private String name;

    private List<Long> checkIds = new ArrayList<>();

}
