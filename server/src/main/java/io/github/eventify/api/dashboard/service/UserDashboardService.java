package io.github.eventify.api.dashboard.service;

import io.github.eventify.api.dashboard.model.OrganizationDashboardSummary;
import io.github.eventify.api.dashboard.model.RecentNotification;
import io.github.eventify.api.dashboard.model.UserDashboard;
import io.github.eventify.api.dashboard.model.WatchlistHealth;
import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.monitor.model.Timeline;
import io.github.eventify.api.monitor.model.TimelineDuration;
import io.github.eventify.api.notification.core.model.Notification;
import io.github.eventify.api.notification.core.repository.NotificationRepository;
import io.github.eventify.api.organization.model.Organization;
import io.github.eventify.api.organization.repository.OrganizationMembershipRepository;
import io.github.eventify.api.watchlist.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.util.TimeProvider.now;
import static org.springframework.data.domain.Sort.Direction.DESC;

/** Service for aggregating user dashboard data. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDashboardService {

    private static final int MAX_NOTIFICATIONS = 10;
    private static final int NOTIFICATION_WINDOW_HOURS = 24;

    private final WatchlistRepository watchlistRepository;
    private final NotificationRepository notificationRepository;
    private final OrganizationMembershipRepository organizationMembershipRepository;

    /**
     * Aggregates dashboard data for the given user.
     *
     * @param userId the user ID
     * @return the aggregated dashboard
     */
    public UserDashboard getDashboard(final Long userId) {
        final List<WatchlistHealth> watchlistHealth = buildWatchlistHealth(userId);
        final List<RecentNotification> recentNotifications = buildRecentNotifications(userId);
        final List<OrganizationDashboardSummary> organizations = buildOrganizations(userId);
        return new UserDashboard(watchlistHealth, recentNotifications, organizations);
    }

    private List<WatchlistHealth> buildWatchlistHealth(final Long userId) {
        return watchlistRepository.findAllByUserId(userId).stream()
            .filter(watchlist -> isAlertingSeverity(resolveWorstSeverity(watchlist.getConfiguration().getTimeline())))
            .map(
                watchlist -> new WatchlistHealth(
                    watchlist.getId(),
                    watchlist.getName(),
                    resolveWorstSeverity(watchlist.getConfiguration().getTimeline()).name(),
                    watchlist.getConfiguration().getAllChannelIds().size()
                )
            )
            .toList();
    }

    private boolean isAlertingSeverity(final Severity severity) {
        return severity != Severity.OK && severity != Severity.NO_DATA;
    }

    private Severity resolveWorstSeverity(final Timeline timeline) {
        if (timeline == null || timeline.getDurations() == null || timeline.getDurations().isEmpty()) {
            return Severity.NO_DATA;
        }
        Severity worst = Severity.NO_DATA;
        for (final TimelineDuration duration : timeline.getDurations()) {
            worst = Severity.worst(worst, duration.getSeverity());
        }
        return worst;
    }

    private List<RecentNotification> buildRecentNotifications(final Long userId) {
        final List<Notification> notifications = notificationRepository.findRecentByUserId(
            userId,
            now().minusHours(NOTIFICATION_WINDOW_HOURS),
            PageRequest.of(0, MAX_NOTIFICATIONS, Sort.by(DESC, "createdAt"))
        );
        return notifications.stream()
            .map(
                notification -> new RecentNotification(
                    notification.getId(),
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.getCategory() != null ? notification.getCategory().name() : null,
                    notification.isUrgent(),
                    notification.getCreatedAt(),
                    notification.getActionUrl()
                )
            )
            .toList();
    }

    private List<OrganizationDashboardSummary> buildOrganizations(final Long userId) {
        return organizationMembershipRepository.findAllByUserIdWithOrganization(userId).stream()
            .map(
                membership -> {
                    final Organization org = membership.getOrganization();
                    return new OrganizationDashboardSummary(
                        org.getId(),
                        org.getName(),
                        org.getStatus() != null ? org.getStatus().name() : null,
                        membership.getRole() != null ? membership.getRole().name() : null,
                        0L,
                        0
                    );
                }
            )
            .toList();
    }
}
