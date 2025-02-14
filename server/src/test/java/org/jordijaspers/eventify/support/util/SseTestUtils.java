package org.jordijaspers.eventify.support.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.test.web.servlet.MvcResult;

import static java.util.Objects.nonNull;

@Slf4j
public final class SseTestUtils {

    private static final String EVENT_PREFIX = "event:";
    private static final String DATA_PREFIX = "data:";

    private static final int EVENT_PREFIX_LENGTH = EVENT_PREFIX.length();
    private static final int DATA_PREFIX_LENGTH = DATA_PREFIX.length();

    private SseTestUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Collects SSE events from a response stream until a specific event type is encountered or timeout occurs.
     *
     * @param mvcResult         The MvcResult containing the SSE stream
     * @param expectedEventType The event type to wait for
     * @param timeout           Maximum duration to wait for the expected event
     * @return List of collected events as key-value maps
     */
    public static List<Map<String, String>> collectEvents(final MvcResult mvcResult,
        final String expectedEventType,
        final Duration timeout) {
        final EventCollector collector = new EventCollector(expectedEventType);
        return collector.collectEvents(mvcResult, timeout);
    }

    private static final class EventCollector {

        private final String expectedEventType;
        private final List<Map<String, String>> events;
        private final CountDownLatch eventReceivedLatch;

        private EventCollector(final String expectedEventType) {
            this.expectedEventType = expectedEventType;
            this.events = Collections.synchronizedList(new ArrayList<>());
            this.eventReceivedLatch = new CountDownLatch(1);
        }

        private List<Map<String, String>> collectEvents(final MvcResult response, final Duration timeout) {
            log.debug("The response stream is open and ready for reading");
            final CompletableFuture<?> future = startEventCollection(response);

            if (!awaitEvent(timeout)) {
                future.cancel(true);
                log.error("Expected event '{}' not received within the timeout", expectedEventType);
                throw new AssertionError("Expected event not received within the timeout");
            }

            return events;
        }

        private CompletableFuture<?> startEventCollection(final MvcResult response) {
            return CompletableFuture.runAsync(() -> {
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(response.getResponse().getContentAsByteArray());
                try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    processEvents(reader);
                } catch (final IOException exception) {
                    log.error("Failed to read SSE events", exception);
                }
            });
        }

        private void processEvents(final BufferedReader reader) throws IOException {
            Map<String, String> event;
            while ((event = readSingleEvent(reader)) != null) {
                log.debug("Received event: {}", event);
                events.add(event);
                if (expectedEventType.equals(event.get("event"))) {
                    eventReceivedLatch.countDown();
                }
            }
        }

        private boolean awaitEvent(final Duration timeout) {
            try {
                return eventReceivedLatch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (final InterruptedException exception) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while waiting for the expected event", exception);
                throw new AssertionError("Interrupted while waiting for the expected event");

            }
        }

        private static Map<String, String> readSingleEvent(final BufferedReader reader) throws IOException {
            final Map<String, String> eventData = new HashMap<>();
            String line;

            while (nonNull(line = reader.readLine())) {
                if (line.isEmpty()) {
                    return eventData.isEmpty()
                        ? null
                        : eventData;
                }

                final String trimmedLine = line.trim();
                if (trimmedLine.startsWith(EVENT_PREFIX)) {
                    eventData.put("event", trimmedLine.substring(EVENT_PREFIX_LENGTH).trim());
                } else if (trimmedLine.startsWith(DATA_PREFIX)) {
                    eventData.put("data", trimmedLine.substring(DATA_PREFIX_LENGTH).trim());
                }
            }

            return eventData.isEmpty()
                ? null
                : eventData;
        }
    }
}
