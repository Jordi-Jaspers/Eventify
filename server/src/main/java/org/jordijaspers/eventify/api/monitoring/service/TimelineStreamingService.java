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
import org.jordijaspers.eventify.api.monitoring.model.SubscriptionData;
import org.jordijaspers.eventify.api.monitoring.model.SubscriptionKey;
import org.jordijaspers.eventify.api.monitoring.model.response.TimelineResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.jordijaspers.eventify.api.monitoring.service.TimelineConsolidator.calculateTimeline;
import static org.jordijaspers.eventify.common.constants.Constants.ServerEvents.INITIALIZED;
import static org.jordijaspers.eventify.common.constants.Constants.ServerEvents.UPDATED;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineStreamingService {

    private final DashboardService dashboardService;

    private final TimelineInitializer timelineInitializer;

    private final TimelineEventHandler timelineEventHandler;

    private final Map<SubscriptionKey, SubscriptionData> activeSubscriptions = new ConcurrentHashMap<>();

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
        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        final SubscriptionData subscriptionData = activeSubscriptions.compute(
            key,
            (subscriptionKey, data) -> {
                if (nonNull(data)) {
                    log.info("Adding emitter to existing subscription for dashboardId: '{}'", dashboardId);
                    data.addEmitter(emitter);
                    return data;
                } else {
                    log.info("Creating new subscription for dashboardId: '{}'", dashboardId);
                    final DashboardSubscription subscription = initializeSubscription(subscriptionKey);
                    timelineInitializer.initializeTimelines(subscription);
                    return new SubscriptionData(emitter, subscription);
                }
            }
        );

        setupEmitterCallbacks(key, emitter, subscriptionData);
        subscriptionData.emitEvents(INITIALIZED);

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
        final List<SubscriptionData> relevantSubscriptions = getRelevantSubscriptions(checkId, events.getFirst());
        if (isEmpty(relevantSubscriptions)) {
            return;
        }

        final TimelineResponse timeline = calculateTimeline(events);
        if (isNull(timeline)) {
            return;
        }

        relevantSubscriptions.forEach(data -> {
            final DashboardSubscription subscription = data.getSubscription();
            if (timelineEventHandler.processTimeline(timeline, checkId, subscription)) {
                data.setSubscription(subscription);
                data.emitEvents(UPDATED);
            }
        });
    }

    /**
     * Initialize a new subscription for the given dashboard and time window.
     *
     * @param key The key of the subscription
     * @return The initialized subscription
     */
    protected DashboardSubscription initializeSubscription(final SubscriptionKey key) {
        final Dashboard dashboard = dashboardService.getDashboardConfiguration(key.getDashboardId());
        return new DashboardSubscription(dashboard, key.getWindow());
    }

    /**
     * Set up the emitter callbacks for the given subscription key and emitter.
     *
     * @param key     The key of the subscription
     * @param emitter The emitter to set up the callbacks for
     * @param data    The subscription data to use for the callbacks
     */
    protected void setupEmitterCallbacks(final SubscriptionKey key, final SseEmitter emitter, final SubscriptionData data) {
        emitter.onCompletion(() -> {
            log.info("SSE connection completed for dashboardId: {}", key.getDashboardId());
            removeEmitter(key, emitter, data);
        });
        emitter.onTimeout(() -> {
            log.warn("SSE connection timed out for dashboardId: {}", key.getDashboardId());
            removeEmitter(key, emitter, data);
        });
        emitter.onError(exception -> {
            log.error("SSE error for dashboardId: {}", key.getDashboardId(), exception);
            removeEmitter(key, emitter, data);
        });
    }

    /**
     * Remove the emitter from the subscription data and remove the subscription data if no emitters are left.
     *
     * @param key              The key of the subscription
     * @param emitter          The emitter to remove
     * @param subscriptionData The subscription data to remove the emitter from
     */
    protected void removeEmitter(final SubscriptionKey key, final SseEmitter emitter, final SubscriptionData subscriptionData) {
        subscriptionData.removeEmitter(emitter);
        if (subscriptionData.hasNoEmitters()) {
            activeSubscriptions.remove(key);
            log.info("Removed subscription data for dashboardId: {}", key.getDashboardId());
        }
    }

    /**
     * Get the relevant subscriptions for the given check and event.
     *
     * @param checkId The id of the check to get the subscriptions for
     * @param event   The event to get the subscriptions for
     * @return The relevant subscriptions
     */
    protected List<SubscriptionData> getRelevantSubscriptions(final Long checkId, final EventRequest event) {
        return activeSubscriptions.values()
            .stream()
            .filter(data -> data.getSubscription().containsCheck(checkId))
            .filter(data -> data.getSubscription().isInWindow(event))
            .toList();
    }

    /**
     * Remove the subscription for the given key.
     *
     * @param key The key of the subscription to remove
     */
    protected void removeSubscription(final SubscriptionKey key) {
        activeSubscriptions.remove(key);
    }

    /**
     * Get the number of active subscriptions.
     *
     * @return The number of active subscriptions
     */
    protected int getActiveSubscriptions() {
        return activeSubscriptions.size();
    }
}
