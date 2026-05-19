package io.github.eventify.api.admin.stats.model;

import io.github.eventify.api.admin.stats.model.response.GrowthDataPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** Domain class holding admin dashboard statistics. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminStats {

    private Long totalOrganizations;
    private Long totalUsers;
    private Long activeUsers;
    private List<GrowthDataPoint> growthData;
    private Long totalChannels;
    private Long activeChannels;
    private Long pausedChannels;
    private Long staleChannels;
    private Long pendingDeletionChannels;
    private Long totalEventsInPeriod;
    private GrowthDataPoint bestGrowthDayUsers;
    private GrowthDataPoint bestGrowthDayOrganizations;
    private GrowthDataPoint bestGrowthDayEvents;
}
