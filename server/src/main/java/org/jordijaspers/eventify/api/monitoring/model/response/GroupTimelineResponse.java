package org.jordijaspers.eventify.api.monitoring.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.jordijaspers.eventify.api.dashboard.model.DashboardGroup;

@Data
@NoArgsConstructor
public class GroupTimelineResponse {

    private Long id;

    private String name;

    private TimelineResponse timeline;

    private List<CheckTimelineResponse> checks = new ArrayList<>();

    /**
     * A default constructor to create an existing group timeline.
     *
     * @param group          The dashboard group containing the checks.
     * @param checkTimelines The check timelines in the group.
     */
    public GroupTimelineResponse(final DashboardGroup group, final List<CheckTimelineResponse> checkTimelines) {
        this.id = group.getId();
        this.name = group.getName();
        this.checks = checkTimelines;
    }

    /**
     * Check if the group contains a check with the given id.
     *
     * @param checkId The id of the check to check for.
     * @return true if the group contains the check, false otherwise.
     */
    public boolean containsCheck(final Long checkId) {
        return this.checks.stream().anyMatch(check -> check.getId().equals(checkId));
    }

}
