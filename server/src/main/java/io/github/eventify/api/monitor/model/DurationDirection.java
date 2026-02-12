package io.github.eventify.api.monitor.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Direction for fetching duration details relative to a timestamp.
 */
@Getter
@AllArgsConstructor
@Schema(description = "DurationDirection")
public enum DurationDirection {

    /**
     * Fetch durations centered around the timestamp.
     */
    AROUND,

    /**
     * Fetch durations before the timestamp.
     */
    BEFORE,

    /**
     * Fetch durations after the timestamp.
     */
    AFTER

}
