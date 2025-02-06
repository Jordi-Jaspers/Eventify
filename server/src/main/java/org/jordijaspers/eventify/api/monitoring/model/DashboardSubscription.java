package org.jordijaspers.eventify.api.monitoring.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.hawaiiframework.repository.DataNotFoundException;
import org.jordijaspers.eventify.api.check.model.Check;
import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.model.DashboardGroup;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.monitoring.model.response.CheckTimelineResponse;
import org.jordijaspers.eventify.api.monitoring.model.response.GroupTimelineResponse;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineResponse;

import static org.jordijaspers.eventify.common.exception.ApiErrorCode.CHECK_NOT_FOUND_ERROR;

@Data
@Slf4j
@NoArgsConstructor
public class DashboardSubscription {

    private Long dashboardId;

    private Long window;

    private TimelineResponse timeline;

    private List<GroupTimelineResponse> groupedChecks = new ArrayList<>();

    private List<CheckTimelineResponse> ungroupedChecks = new ArrayList<>();

    /**
     * Create a new dashboard subscription.
     *
     * @param dashboard  The dashboard to subscribe to
     * @param timeWindow The time window to use for the subscription
     */
    public DashboardSubscription(final Dashboard dashboard, final Duration timeWindow) {
        this.window = timeWindow.toMinutes();
        this.dashboardId = dashboard.getId();
        this.groupedChecks = mapGroupedChecks(dashboard.getGroupedChecks());
        this.ungroupedChecks = mapUngroupedChecks(dashboard.getUngroupedChecks());
    }

    /**
     * Retrieve all the check configured in the dashboard. (Grouped and ungrouped)
     *
     * @return the checks
     */
    public List<CheckTimelineResponse> getAllChecks() {
        final List<CheckTimelineResponse> checks = this.groupedChecks.stream()
            .map(GroupTimelineResponse::getChecks)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        checks.addAll(this.ungroupedChecks);
        return checks;
    }

    /**
     * Get the check IDs for all checks in the dashboard.
     *
     * @return the check IDs
     */
    public Set<Long> getAllCheckIds() {
        return this.getAllChecks().stream()
            .map(CheckTimelineResponse::getId)
            .collect(Collectors.toSet());
    }

    /**
     * Find the group that contains the given check.
     *
     * @param checkId The ID of the check to find
     * @return The group containing the check
     */
    public Optional<GroupTimelineResponse> findAffectedGroup(final Long checkId) {
        return this.groupedChecks.stream()
            .filter(group -> group.containsCheck(checkId))
            .findFirst();
    }

    /**
     * Find the check that is affected by the event.
     *
     * @param checkId The ID of the check to find
     * @return The check that is affected
     */
    public CheckTimelineResponse findAffectedCheck(final Long checkId) {
        return getAllChecks().stream()
            .filter(check -> check.getId().equals(checkId))
            .findFirst()
            .orElseThrow(() -> new DataNotFoundException(CHECK_NOT_FOUND_ERROR));
    }

    /**
     * Check if the subscription contains the check with the given ID.
     *
     * @param checkId The ID of the check to check for
     * @return True if the check is in the subscription, false otherwise
     */
    public boolean containsCheck(final Long checkId) {
        return this.getAllChecks().stream()
            .anyMatch(check -> check.getId().equals(checkId));
    }

    /**
     * Check if the event is within the subscription window.
     *
     * @param event The event to check
     * @return True if the event is within the window, false otherwise
     */
    public boolean isInWindow(final EventRequest event) {
        final ZonedDateTime eventTime = event.getTimestamp();
        final ZonedDateTime windowStart = ZonedDateTime.now().minusHours(this.window);
        return !eventTime.isBefore(windowStart);
    }

    private static List<CheckTimelineResponse> toCheckTimelines(final Set<Check> checks) {
        return checks.stream()
            .map(CheckTimelineResponse::new)
            .toList();
    }

    private static List<CheckTimelineResponse> mapUngroupedChecks(final Set<Check> checks) {
        return toCheckTimelines(checks);
    }

    private static List<GroupTimelineResponse> mapGroupedChecks(final Map<DashboardGroup, Set<Check>> groupedChecks) {
        return groupedChecks.entrySet().stream()
            .map(
                entry -> new GroupTimelineResponse(
                    entry.getKey(),
                    toCheckTimelines(entry.getValue())
                )
            )
            .toList();
    }
}
