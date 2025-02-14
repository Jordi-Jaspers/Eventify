package org.jordijaspers.eventify.api.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.jordijaspers.eventify.api.event.model.EventBatch;
import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.common.exception.EventPublishingException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.jordijaspers.eventify.common.config.RabbitMQConfig.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private final Map<Long, LinkedBlockingQueue<EventRequest>> checkQueues = new ConcurrentHashMap<>();

    /**
     * Publish an event to the RabbitMQ exchange. The event will be routed to the correct queue based on the check id. The event will be
     * published as JSON. The routing key will be 'monitoring.event.{checkId}'.
     *
     * @param event The event to publish
     */
    public void publish(final EventRequest event) {
        final LinkedBlockingQueue<EventRequest> queue = checkQueues.computeIfAbsent(
            event.getCheckId(),
            k -> new LinkedBlockingQueue<>(QUEUE_CAPACITY)
        );

        if (!queue.offer(event)) {
            log.warn("Queue full for check '{}', dropping event", event.getCheckId());
        }
    }

    /**
     * Publish all events in the queues to the RabbitMQ exchange. The events will be published in batches of 1000 events. The batch will be
     * published every second, regardless of the number of events in the queue.
     */
    @Scheduled(
        fixedRate = 5,
        timeUnit = SECONDS
    )
    public void publishBatches() {
        checkQueues.forEach((checkId, queue) -> {
            final List<EventRequest> batch = new ArrayList<>(BATCH_SIZE);
            queue.drainTo(batch, BATCH_SIZE);

            if (!batch.isEmpty()) {
                final EventBatch eventBatch = new EventBatch(checkId, batch);
                try {
                    rabbitTemplate.convertAndSend(EXCHANGE_NAME, ROUTING_KEY_PREFIX + checkId, eventBatch);
                    log.debug("Published batch of '{}' events for check '{}'", batch.size(), checkId);
                } catch (final AmqpException exception) {
                    log.error("Failed to publish batch of '{}' events for check '{}'", batch.size(), checkId, exception);
                    throw new EventPublishingException(exception);
                }
            }
        });
    }
}
