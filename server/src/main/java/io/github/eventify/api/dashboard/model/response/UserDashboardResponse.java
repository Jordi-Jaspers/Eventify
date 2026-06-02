package io.github.eventify.api.dashboard.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/** Response DTO for the user dashboard aggregation endpoint. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Aggregated user dashboard data")
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class UserDashboardResponse {

    @Schema(
        description = "Watchlist health summaries",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<WatchlistHealthResponse> watchlistHealth;

    @Schema(
        description = "Recent notifications within the last 24 hours",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<RecentNotificationResponse> recentNotifications;

    @Schema(
        description = "Organization membership summaries",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<OrganizationStatusResponse> organizations;

    /** Returns the watchlist health items. */
    public List<WatchlistHealthResponse> watchlistHealth() {
        return watchlistHealth;
    }

    /** Returns the recent notification items. */
    public List<RecentNotificationResponse> recentNotifications() {
        return recentNotifications;
    }

    /** Returns the organization status items. */
    public List<OrganizationStatusResponse> organizations() {
        return organizations;
    }
}
