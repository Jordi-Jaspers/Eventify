package org.jordijaspers.eventify.support.util;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
final class SseTestClient {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final ObjectMapper objectMapper;

    private volatile boolean isRunning;

    private Thread sseThread;

    public <T> T waitForEvent(
        final String path,
        final Map<String, String> queryParams,
        final Map<String, String> headers,
        final String expectedEventType,
        final Class<T> dataType
    ) throws Exception {
        return waitForEvent(path, queryParams, headers, expectedEventType, dataType, DEFAULT_TIMEOUT);
    }

    public <T> T waitForEvent(
        final String path,
        final Map<String, String> queryParams,
        final Map<String, String> headers,
        final String expectedEventType,
        final Class<T> dataType,
        final Duration timeout
    ) throws Exception {
        final CountDownLatch eventLatch = new CountDownLatch(1);
        final AtomicReference<T> resultRef = new AtomicReference<>();
        final AtomicReference<Exception> errorRef = new AtomicReference<>();

        startEventStream(
            path,
            queryParams,
            headers,
            event -> {
                if (expectedEventType.equals(event.get("event"))) {
                    try {
                        final T data = objectMapper.readValue(event.get("data"), dataType);
                        resultRef.set(data);
                        eventLatch.countDown();
                    } catch (final Exception e) {
                        errorRef.set(e);
                        eventLatch.countDown();
                    }
                }
            }
        );

        final boolean received = eventLatch.await(timeout.getSeconds(), TimeUnit.SECONDS);
        if (!received) {
            throw new AssertionError("Timeout waiting for event: " + expectedEventType);
        }

        if (errorRef.get() != null) {
            throw errorRef.get();
        }

        return resultRef.get();
    }

    public void startEventStream(
        final String path,
        final Map<String, String> queryParams,
        final Map<String, String> headers,
        final Consumer<Map<String, String>> eventHandler
    ) {
        if (isRunning) {
            throw new IllegalStateException("SSE client is already running");
        }

        isRunning = true;
        sseThread = new Thread(() -> {
            final RequestSpecification request = RestAssured.given()
                .baseUri(baseUri)
                .port(port)
                .accept(MediaType.TEXT_EVENT_STREAM_VALUE);

            // Add query parameters
            queryParams.forEach(request::queryParam);

            // Add headers
            headers.forEach(request::header);

            request
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .contentType(MediaType.TEXT_EVENT_STREAM_VALUE)
                .body(response -> {
                    final SseEventParser parser = new SseEventParser(response.asString());
                    while (isRunning) {
                        final Map<String, String> event = parser.parseNextEvent();
                        if (!event.isEmpty()) {
                            eventHandler.accept(event);
                        }
                    }
                });
        });

        sseThread.start();
    }

    public void stop() {
        isRunning = false;
        if (sseThread != null) {
            sseThread.interrupt();
            sseThread = null;
        }
    }
}
