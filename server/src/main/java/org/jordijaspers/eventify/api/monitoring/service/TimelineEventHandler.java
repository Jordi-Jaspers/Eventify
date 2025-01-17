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
import static org.jordijaspers.eventify.api.monitoring.service.TimelineConsolidator.consolidateTimeline;

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
    public void processEvent(final EventRequest event, final DashboardSubscription subscription) {
        final CheckTimelineResponse affectedCheck = subscription.findAffectedCheck(event);

        if (updateTimeline(affectedCheck.getTimelineResponse(), event)) {
            final Optional<GroupTimelineResponse> affectedGroup = subscription.findAffectedGroup(event.getCheckId());

            if (affectedGroup.isPresent()) {
                updateGroupedCheckHierarchy(affectedGroup.get(), subscription);
            } else {
                updateDashboardTimeline(subscription);
            }
        }
    }

    private boolean updateTimeline(TimelineResponse timeline, final EventRequest event) {
        final TimelineResponse currentTimeline = Optional.ofNullable(timeline)
            .orElseGet(TimelineResponse::new);

        final List<TimelineDurationResponse> durations = currentTimeline.getDurations();
        final boolean shouldUpdate = durations.isEmpty() || !event.getStatus().equals(durations.getLast().getStatus());

        if (shouldUpdate) {
            if (!durations.isEmpty() && isNull(durations.getLast().getEndTime())) {
                durations.getLast().setEndTime(event.getTimestamp());
            }
            durations.add(new TimelineDurationResponse(event.getTimestamp(), event.getStatus()));
        }

        return shouldUpdate;
    }

    private void updateGroupedCheckHierarchy(final GroupTimelineResponse group, final DashboardSubscription subscription) {
        final List<TimelineResponse> checkTimelines = group.getChecks().stream()
            .map(CheckTimelineResponse::getTimelineResponse)
            .toList();

        consolidateTimeline(checkTimelines, group::setTimelineResponse);
        updateDashboardTimeline(subscription);
    }

    private void updateDashboardTimeline(final DashboardSubscription subscription) {
        final Stream<TimelineResponse> groupTimelines = subscription.getGroupedChecks().stream()
            .map(GroupTimelineResponse::getTimelineResponse);

        final Stream<TimelineResponse> ungroupedTimelines = subscription.getUngroupedChecks().stream()
            .map(CheckTimelineResponse::getTimelineResponse);

        consolidateTimeline(concat(groupTimelines, ungroupedTimelines).toList(), subscription::setTimeline);
    }
}
