package io.github.eventify.api.event.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * Response after creating an event.
 */
@Getter
@AllArgsConstructor
public class EventCreatedResponse {

    private final Long id;

    private final OffsetDateTime timestamp;

}
