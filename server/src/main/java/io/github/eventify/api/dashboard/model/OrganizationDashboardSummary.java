package io.github.eventify.api.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Organization dashboard summary. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationDashboardSummary {

    private Long id;
    private String name;
    private String status;
    private String role;
    private long eventVolumeToday;
    private int channelsInAlertCount;
}
