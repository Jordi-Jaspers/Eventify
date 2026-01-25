package io.github.eventify.api.monitor.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Time ranges for monitor queries.
 * Preset ranges calculate duration automatically.
 * CUSTOM requires explicit startTime/endTime.
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "Time range preset or custom")
public enum TimeRange {

    LAST_24H("24h", Duration.ofHours(24)),
    LAST_7D("7d", Duration.ofDays(7)),
    LAST_30D("30d", Duration.ofDays(30)),
    CUSTOM("custom", null);

    @JsonValue
    private final String value;

    private final Duration duration;

    /**
     * Returns true if this is a live/preset range (not CUSTOM).
     */
    public boolean isLive() {
        return this != CUSTOM;
    }

    /**
     * Creates TimeRange from JSON value.
     */
    @JsonCreator
    public static TimeRange fromValue(final String value) {
        for (final TimeRange range : values()) {
            if (range.value.equals(value)) {
                return range;
            }
        }
        throw new IllegalArgumentException("Unknown TimeRange: " + value);
    }
}
