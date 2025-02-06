package org.jordijaspers.eventify.api.monitoring.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jordijaspers.eventify.common.exception.DashboardStreamingException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static java.util.Objects.nonNull;

@Data
@Slf4j
@NoArgsConstructor
public class SubscriptionData {

    private List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private DashboardSubscription subscription;

    /**
     * Create a new subscription data object.
     *
     * @param emitter      The emitter to use for the subscription
     * @param subscription The subscription to use for the subscription
     */
    public SubscriptionData(final SseEmitter emitter, final DashboardSubscription subscription) {
        this.emitters = new CopyOnWriteArrayList<>(List.of(emitter));
        this.subscription = subscription;
    }

    /**
     * Send an event with the given name to the dashboard subscription.
     *
     * @param name The name of the event to send
     */
    public void emitEvents(final String name) {
        try {
            for (final SseEmitter emitter : emitters) {
                emitter.send(SseEmitter.event().name(name).data(this.subscription));
            }
        } catch (final IOException exception) {
            log.error("Failed to emit the event for dashboard subscription '{}'", this.subscription.getDashboardId(), exception);
            throw new DashboardStreamingException();
        }
    }

    /**
     * Add an emitter to the subscription data.
     *
     * @param emitter The emitter to add
     */
    public void addEmitter(final SseEmitter emitter) {
        if (nonNull(emitter) && !emitters.contains(emitter)) {
            emitters.add(emitter);
        }
    }

    /**
     * Remove an emitter from the subscription data.
     *
     * @param emitter The emitter to remove
     */
    public void removeEmitter(final SseEmitter emitter) {
        if (nonNull(emitter)) {
            emitters.remove(emitter);
        }
    }

    /**
     * Check if the subscription data has no emitters left.
     *
     * @return True if there are no emitters left, false otherwise
     */
    public boolean hasNoEmitters() {
        return emitters.isEmpty();
    }
}
