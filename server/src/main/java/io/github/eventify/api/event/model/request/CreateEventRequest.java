package io.github.eventify.api.event.model.request;

import io.github.eventify.api.event.model.Severity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Request for creating an event.
 * Used for both single event ingestion and batch ingestion.
 * For single events, timestamp is optional (server-generated if not provided).
 * For batch events, timestamp is required.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class CreateEventRequest {

    private String slug;
    private Severity severity;
    private String title;
    private String message;
    private Map<String, Object> metadata;
    private OffsetDateTime timestamp;
}
