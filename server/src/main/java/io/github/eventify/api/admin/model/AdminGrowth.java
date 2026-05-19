package io.github.eventify.api.admin.model;

import io.github.eventify.api.admin.model.response.GrowthDataPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Domain object holding growth data for a time window. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminGrowth {

    private List<GrowthDataPoint> growthData;

    private GrowthDataPoint bestGrowthDayUsers;

    private GrowthDataPoint bestGrowthDayOrganizations;

    private GrowthDataPoint bestGrowthDayEvents;
}
