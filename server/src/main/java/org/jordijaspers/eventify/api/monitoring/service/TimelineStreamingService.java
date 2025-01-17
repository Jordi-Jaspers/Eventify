package org.jordijaspers.eventify.api.monitoring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.service.DashboardService;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.monitoring.model.DashboardSubscription;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineStreamingService {

    private final DashboardService dashboardService;

    private final TimelineEventHandler timelineEventHandler;

    private final TimelineInitializer timelineInitializer;

    private final Map<Long, DashboardSubscription> dashboardSubscriptions = new ConcurrentHashMap<>();

    /**
     * Subscribe to a dashboard and receive real-time updates. The subscription will be kept alive until the client disconnects and the
     * timelines contains the initial state of the dashboard for the specified time window.
     *
     * @param dashboardId The id of the dashboard to subscribe to
     * @param window      The time window to use for the subscription
     * @return The emitter to use for the subscription
     */
    @Transactional(readOnly = true)
    public SseEmitter subscribe(final Long dashboardId, final Optional<Duration> window) {
        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        final Duration timeWindow = window.orElse(Duration.ofHours(72));
        final Dashboard dashboard = dashboardService.getDashboardConfiguration(dashboardId);

        // TODO: Check if there is already a subscription for this dashboard with the same time window. If so, return the existing emitter.
        final DashboardSubscription subscription = new DashboardSubscription(dashboard, timeWindow, emitter);
        dashboardSubscriptions.put(dashboardId, subscription);
        setupEmitterCallbacks(dashboardId, emitter);

        timelineInitializer.initializeTimelines(subscription);
        subscription.emitEvent("INITIALIZED");
        return emitter;
    }

    /**
     * Event handler for incoming events. This will process the event and update the timeline for all relevant subscriptions.
     *
     * @param event The event to process
     */
    public void handleEvent(final EventRequest event) {
        dashboardSubscriptions.values().stream()
            .filter(subscription -> subscription.isEventRelevant(event))
            .forEach(subscription -> {
                timelineEventHandler.processEvent(event, subscription);
                subscription.emitEvent("UPDATED");
            });

    }

    private void setupEmitterCallbacks(final Long dashboardId, final SseEmitter emitter) {
        emitter.onCompletion(() -> removeSubscription(dashboardId));
        emitter.onTimeout(() -> removeSubscription(dashboardId));
        emitter.onError(e -> removeSubscription(dashboardId));
    }

    private void removeSubscription(final Long dashboardId) {
        dashboardSubscriptions.remove(dashboardId);
    }
}
