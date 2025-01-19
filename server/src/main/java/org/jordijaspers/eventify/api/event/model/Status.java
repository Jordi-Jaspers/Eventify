package org.jordijaspers.eventify.api.event.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Comparator;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum Status {

    OK("Service is responding normally", true, 0),
    DEGRADED("Service is responding slowly", true, 1),
    MAINTENANCE("Service is under maintenance", true, 2),
    WARNING("Service is responding with errors", true, 3),
    CRITICAL("Service is not responding", true, 4),
    DETACHED("Service is not attached to the monitoring system", false, -1),
    UNKNOWN("The status of the service is unknown", false, -1);

    private final String description;

    private final boolean considerForWorst;

    private final int severity;

    /**
     * Check if the status is OK.
     *
     * @return True if the status is OK, false otherwise
     */
    public boolean isNotOk() {
        return this != OK;
    }

    /**
     * Get the worst status of two statuses based on severity and consideration flags.
     *
     * @param a The first status
     * @param b The second status
     * @return The worst status of the two
     */
    public static Status worst(final Status a, final Status b) {
        return Stream.of(a, b)
            .filter(status -> status.considerForWorst)
            .max(Comparator.comparingInt(status -> status.severity))
            .orElseGet(() -> a.considerForWorst ? a : b);
    }
}
