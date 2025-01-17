package org.jordijaspers.eventify.api.monitoring.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
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
import org.jordijaspers.eventify.common.exception.DashboardStreamingException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.jordijaspers.eventify.common.exception.ApiErrorCode.CHECK_NOT_FOUND_ERROR;

@Data
@Slf4j
@NoArgsConstructor
public class DashboardSubscription {

    private SseEmitter emitter;

    private Long dashboardId;

    private Duration window;

    private TimelineResponse timeline;

    private List<GroupTimelineResponse> groupedChecks = new ArrayList<>();

    private List<CheckTimelineResponse> ungroupedChecks = new ArrayList<>();

    /**
     * Create a new dashboard subscription.
     *
     * @param dashboard  The dashboard to subscribe to
     * @param timeWindow The time window to use for the subscription
     * @param emitter    The emitter to use for the subscription
     */
    public DashboardSubscription(final Dashboard dashboard, final Duration timeWindow, final SseEmitter emitter) {
        this.emitter = emitter;
        this.window = timeWindow;
        this.dashboardId = dashboard.getId();
        this.groupedChecks = mapGroupedChecks(dashboard.getGroupedChecks());
        this.ungroupedChecks = mapUngroupedChecks(dashboard.getUngroupedChecks());
    }

    /**
     * Send an event with the given name to the dashboard subscription.
     *
     * @param name The name of the event to send
     */
    public void emitEvent(final String name) {
        try {
            this.emitter.send(SseEmitter.event().name(name).data(this));
        } catch (final IOException exception) {
            log.error("Failed to emit the event for dashboard subscription '{}'", this.dashboardId, exception);
            throw new DashboardStreamingException();
        }
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
     * @param event The event to find the check for
     * @return The check that is affected
     */
    public CheckTimelineResponse findAffectedCheck(final EventRequest event) {
        return getAllChecks().stream()
            .filter(check -> check.getId().equals(event.getCheckId()))
            .findFirst()
            .orElseThrow(() -> new DataNotFoundException(CHECK_NOT_FOUND_ERROR));
    }

    /**
     * Check if the event is relevant for the subscription. The event is relevant if it is part of the subscription and within the window.
     *
     * @param event The event to check
     * @return True if the event is relevant, false otherwise
     */
    public boolean isEventRelevant(final EventRequest event) {
        return containsCheck(event) && isInWindow(event);
    }

    private boolean containsCheck(final EventRequest event) {
        return this.getAllChecks().stream()
            .anyMatch(check -> check.getId().equals(event.getCheckId()));
    }

    private boolean isInWindow(final EventRequest event) {
        final ZonedDateTime eventTime = event.getTimestamp();
        final ZonedDateTime windowStart = ZonedDateTime.now().minus(this.window);
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
