package org.jordijaspers.eventify.api.dashboard.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * Request object to create a new dashboard group.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class DashboardGroupRequest {

    private String name;

    private List<Long> checkIds = new ArrayList<>();

}
