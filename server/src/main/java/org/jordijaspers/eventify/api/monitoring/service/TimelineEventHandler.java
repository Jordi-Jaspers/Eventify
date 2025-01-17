package org.jordijaspers.eventify.api.monitoring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.monitoring.model.DashboardSubscription;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineEventHandler {

    /**
     * Process the event and update the timeline for the given subscription.
     *
     * @param event        The event to process
     * @param subscription The subscription to update
     */
    public void processEvent(final EventRequest event, final DashboardSubscription subscription) {
        // Find the check that is affected by the event
        subscription.findAffectedCheck(event);

        // Insert the event in the timeline
        // if the event does not change the status at the specified timestamp, the timeline will not be updated and return empty

        // If the event triggered a status change, update the timeline

        // Check if the event belongs to a group and update the group timeline if necessary

        // update the dashboard with the new timeline

    }

}
