package io.github.eventify.api.event.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * Enum representing the severity level of an event.
 */
@Getter
@AllArgsConstructor
@Schema(description = "Severity")
public enum Severity {

    OK,
    WARNING,
    CRITICAL;

    /**
     * Retrieve all the configured permissions as a stream.
     *
     * @return A stream of the configured permissions.
     */
    public Stream<Severity> stream() {
        return Stream.of(values());
    }
}
