package io.github.eventify.api.dashboard.model.mapper;

import io.github.eventify.api.dashboard.model.OrganizationDashboardSummary;
import io.github.eventify.api.dashboard.model.RecentNotification;
import io.github.eventify.api.dashboard.model.UserDashboard;
import io.github.eventify.api.dashboard.model.WatchlistHealth;
import io.github.eventify.api.dashboard.model.response.OrganizationStatusResponse;
import io.github.eventify.api.dashboard.model.response.RecentNotificationResponse;
import io.github.eventify.api.dashboard.model.response.UserDashboardResponse;
import io.github.eventify.api.dashboard.model.response.WatchlistHealthResponse;
import io.github.jframe.util.mapper.config.SharedMapperConfig;

import org.mapstruct.Mapper;

/**
 * MapStruct mapper for converting UserDashboard domain object to UserDashboardResponse DTO.
 */
@Mapper(config = SharedMapperConfig.class)
public abstract class UserDashboardMapper {

    public abstract UserDashboardResponse toResponse(UserDashboard dashboard);

    public abstract WatchlistHealthResponse toWatchlistHealthItem(WatchlistHealth watchlistHealth);

    public abstract RecentNotificationResponse toRecentNotificationItem(RecentNotification recentNotification);

    public abstract OrganizationStatusResponse toOrganizationStatusItem(OrganizationDashboardSummary organizationDashboardSummary);
}
