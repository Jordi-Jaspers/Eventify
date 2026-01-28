package io.github.eventify.api.event.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing the severity level of an event.
 */
@Getter
@AllArgsConstructor
@Schema(description = "Severity")
public enum Severity {

    CRITICAL(0),
    WARNING(1),
    OK(2),
    NO_DATA(999);

    private final int priority;

    /**
     * Returns the worst (highest priority) severity between two.
     * Lower priority number = worse severity.
     *
     * @param a first severity
     * @param b second severity
     * @return the worse severity
     */
    public static Severity worst(final Severity a, final Severity b) {
        final Severity result;
        if (a == null) {
            result = b;
        } else if (b == null) {
            result = a;
        } else {
            result = a.priority < b.priority ? a : b;
        }
        return result;
    }
}
