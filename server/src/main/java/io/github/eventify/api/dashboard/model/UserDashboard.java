package io.github.eventify.api.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** Domain class representing aggregated user dashboard data. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDashboard {

    private List<WatchlistHealth> watchlistHealth;
    private List<RecentNotification> recentNotifications;
    private List<OrganizationDashboardSummary> organizations;
}
