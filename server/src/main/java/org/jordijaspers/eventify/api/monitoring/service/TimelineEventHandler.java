package org.jordijaspers.eventify.api.monitoring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.monitoring.model.DashboardSubscription;
import org.jordijaspers.eventify.api.monitoring.model.response.CheckTimelineResponse;
import org.jordijaspers.eventify.api.monitoring.model.response.GroupTimelineResponse;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineDurationResponse;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineResponse;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;
import static java.util.stream.Stream.concat;
import static org.jordijaspers.eventify.api.monitoring.service.TimelineConsolidator.consolidateTimelines;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineEventHandler {

    /**
     * Process the event and update timelines hierarchically. Updates only propagate if the check's timeline actually changes. For ungrouped
     * checks, only the dashboard timeline is updated.
     *
     * @param event        The event to process
     * @param subscription The subscription to update
     */
    public boolean processEvent(final EventRequest event, final DashboardSubscription subscription) {
        final CheckTimelineResponse affectedCheck = subscription.findAffectedCheck(event.getCheckId());
        if (updateTimeline(affectedCheck, event)) {
            final Optional<GroupTimelineResponse> affectedGroup = subscription.findAffectedGroup(event.getCheckId());

            if (affectedGroup.isPresent()) {
                updateGroupedCheckHierarchy(affectedGroup.get(), subscription);
            } else {
                updateDashboardTimeline(subscription);
            }
            return true;
        }
        return false;
    }

    /**
     * Process the timeline and update timelines hierarchically. Updates only propagate if the check's timeline actually changes. For
     * ungrouped checks, only the dashboard timeline is updated.
     *
     * @param timeline     The timeline to process
     * @param checkId      The check id to update
     * @param subscription The subscription to update
     * @return True if the timeline was updated, false otherwise
     */
    public boolean processTimeline(final TimelineResponse timeline, final Long checkId, final DashboardSubscription subscription) {
        final CheckTimelineResponse affectedCheck = subscription.findAffectedCheck(checkId);
        final TimelineResponse current = affectedCheck.getTimeline();

        final TimelineResponse combinedTimeline = consolidateTimelines(List.of(current, timeline));
        if (current.getDurations().size() == combinedTimeline.getDurations().size()) {
            return false;
        }

        affectedCheck.setTimeline(combinedTimeline);

        final Optional<GroupTimelineResponse> affectedGroup = subscription.findAffectedGroup(checkId);
        if (affectedGroup.isPresent()) {
            updateGroupedCheckHierarchy(affectedGroup.get(), subscription);
        } else {
            updateDashboardTimeline(subscription);
        }
        return true;
    }

    private boolean updateTimeline(final CheckTimelineResponse checkTimeline, final EventRequest event) {
        final TimelineResponse currentTimeline = Optional.ofNullable(checkTimeline.getTimeline())
            .orElseGet(TimelineResponse::new);

        final List<TimelineDurationResponse> durations = currentTimeline.getDurations();
        final boolean shouldUpdate = durations.isEmpty() || !event.getStatus().equals(durations.getLast().getStatus());

        if (shouldUpdate) {
            if (!durations.isEmpty() && isNull(durations.getLast().getEndTime())) {
                durations.getLast().setEndTime(event.getTimestamp());
            }
            durations.add(new TimelineDurationResponse(event.getTimestamp(), event.getStatus()));
            currentTimeline.setDurations(durations);
        }

        checkTimeline.setTimeline(currentTimeline);
        return shouldUpdate;
    }

    private void updateGroupedCheckHierarchy(final GroupTimelineResponse group, final DashboardSubscription subscription) {
        final List<TimelineResponse> checkTimelines = group.getChecks().stream()
            .map(CheckTimelineResponse::getTimeline)
            .toList();

        TimelineConsolidator.consolidateTimelines(checkTimelines, group::setTimeline);
        updateDashboardTimeline(subscription);
    }

    private void updateDashboardTimeline(final DashboardSubscription subscription) {
        final Stream<TimelineResponse> groupTimelines = subscription.getGroupedChecks().stream()
            .map(GroupTimelineResponse::getTimeline);

        final Stream<TimelineResponse> ungroupedTimelines = subscription.getUngroupedChecks().stream()
            .map(CheckTimelineResponse::getTimeline);

        TimelineConsolidator.consolidateTimelines(concat(groupTimelines, ungroupedTimelines).toList(), subscription::setTimeline);
    }
}
