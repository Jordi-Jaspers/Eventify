package org.jordijaspers.eventify.api.monitoring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.service.DashboardService;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.monitoring.model.DashboardSubscription;
import org.jordijaspers.eventify.api.monitoring.model.SubscriptionKey;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.jordijaspers.eventify.api.monitoring.service.TimelineConsolidator.calculateTimeline;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineStreamingService {

    private final DashboardService dashboardService;

    private final TimelineInitializer timelineInitializer;

    private final TimelineEventHandler timelineEventHandler;

    private final Map<SubscriptionKey, DashboardSubscription> dashboardSubscriptions = new ConcurrentHashMap<>();

    /**
     * Subscribe to a dashboard and receive real-time updates. The subscription will be kept alive until the client disconnects and the
     * timelines contains the initial state of the dashboard for the specified time window.
     *
     * @param dashboardId The id of the dashboard to subscribe to
     * @param window      The time window to use for the subscription
     * @return The emitter to use for the subscription
     */
    @Transactional(readOnly = true)
    public SseEmitter subscribe(final Long dashboardId, final Duration window) {
        final SubscriptionKey key = new SubscriptionKey(dashboardId, window);
        final DashboardSubscription existingSubscription = dashboardSubscriptions.get(key);
        if (nonNull(existingSubscription)) {
            log.info("Reusing existing subscription for dashboard '{}'", dashboardId);
            return existingSubscription.getEmitter();
        }

        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        final DashboardSubscription subscription = initializeSubscription(emitter, key);
        timelineInitializer.initializeTimelines(subscription);
        subscription.emitEvent("INITIALIZED");

        return emitter;
    }

    /**
     * Process a batch of events for a single check and update relevant subscriptions. Events in the batch are assumed to be ordered by
     * timestamp.
     *
     * @param events  A sorted list of events to process.
     * @param checkId The id of the check to process the events for
     */
    @SuppressWarnings("ReturnCount")
    public void updateTimelineForCheck(final List<EventRequest> events, final Long checkId) {
        final List<DashboardSubscription> relevantSubscriptions = getRelevantSubscriptions(checkId, events.getFirst());
        if (isEmpty(relevantSubscriptions)) {
            return;
        }

        final TimelineResponse timeline = calculateTimeline(events);
        if (isNull(timeline)) {
            return;
        }

        relevantSubscriptions.forEach(subscription -> {
            if (timelineEventHandler.processTimeline(timeline, checkId, subscription)) {
                subscription.emitEvent("UPDATED");
            }
        });
    }

    /**
     * Initialize a new subscription for the given dashboard and time window.
     *
     * @param emitter The emitter to use for the subscription
     * @param key     The key of the subscription
     * @return The initialized subscription
     */
    protected DashboardSubscription initializeSubscription(final SseEmitter emitter, final SubscriptionKey key) {
        setupEmitterCallbacks(key, emitter);

        final Dashboard dashboard = dashboardService.getDashboardConfiguration(key.getDashboardId());
        final DashboardSubscription subscription = new DashboardSubscription(dashboard, key.getWindow(), emitter);
        dashboardSubscriptions.put(key, subscription);

        return subscription;
    }

    /**
     * Setup the emitter callbacks for the given subscription key and emitter.
     *
     * @param key     The key of the subscription
     * @param emitter The emitter to setup the callbacks for
     */
    protected void setupEmitterCallbacks(final SubscriptionKey key, final SseEmitter emitter) {
        emitter.onCompletion(() -> removeSubscription(key));
        emitter.onTimeout(() -> removeSubscription(key));
        emitter.onError(e -> removeSubscription(key));
    }

    /**
     * Remove the subscription for the given key.
     *
     * @param key The key of the subscription to remove
     */
    protected void removeSubscription(final SubscriptionKey key) {
        dashboardSubscriptions.remove(key);
    }

    /**
     * Get the number of active subscriptions.
     *
     * @return The number of active subscriptions
     */
    protected int getActiveSubscriptions() {
        return dashboardSubscriptions.size();
    }

    /**
     * Get the relevant subscriptions for the given check and event.
     *
     * @param checkId The id of the check to get the subscriptions for
     * @param event   The event to get the subscriptions for
     * @return The relevant subscriptions
     */
    protected List<DashboardSubscription> getRelevantSubscriptions(final Long checkId, final EventRequest event) {
        return dashboardSubscriptions.values()
            .stream()
            .filter(subscription -> subscription.containsCheck(checkId))
            .filter(subscription -> subscription.isInWindow(event))
            .toList();
    }
}
