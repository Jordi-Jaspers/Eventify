package org.jordijaspers.eventify.api.monitoring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jordijaspers.eventify.api.monitoring.model.DashboardSubscription;
import org.jordijaspers.eventify.api.monitoring.model.response.CheckTimelineResponse;
import org.jordijaspers.eventify.api.monitoring.model.response.GroupTimelineResponse;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineResponse;
import org.springframework.stereotype.Service;

import static org.jordijaspers.eventify.api.monitoring.service.TimelineConsolidator.consolidateTimelines;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineInitializer {

    private final TimelineService timelineService;

    /**
     * Initialize the timelines for the given subscription. This will fetch the timelines for all checks and groups. The timelines are
     * calculated by the database based on the given window. The timelines are then stored in the subscription. Afterwards, the timelines
     * are consolidated for the groups and the dashboard itself by merging the timelines and taking their worst status.
     *
     * @param subscription The subscription to initialize the timelines for
     */
    public void initializeTimelines(final DashboardSubscription subscription) {
        final Set<Long> checkIds = subscription.getAllCheckIds();
        final Map<Long, TimelineResponse> checkTimelines = timelineService.getTimelinesForChecks(checkIds, subscription.getWindow());

        subscription.getAllChecks().forEach(check -> check.setTimeline(checkTimelines.get(check.getId())));
        subscription.getGroupedChecks().forEach(group -> {
            final List<TimelineResponse> groupTimelines = group.getChecks().stream()
                .map(CheckTimelineResponse::getTimeline)
                .toList();

            consolidateTimelines(groupTimelines, group::setTimeline);
        });

        final List<TimelineResponse> allTimelines = subscription.getGroupedChecks().stream()
            .map(GroupTimelineResponse::getTimeline)
            .collect(Collectors.toList());

        allTimelines.addAll(
            subscription.getUngroupedChecks().stream()
                .map(CheckTimelineResponse::getTimeline)
                .toList()
        );

        consolidateTimelines(allTimelines, subscription::setTimeline);
    }
}
