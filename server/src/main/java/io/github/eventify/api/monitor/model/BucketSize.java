package io.github.eventify.api.monitor.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * LOD bucket size for aggregate timeline queries.
 */
@Getter
@RequiredArgsConstructor
@Schema(description = "LOD bucket size for aggregate timeline queries")
public enum BucketSize {

    PT30M("PT30M", Duration.ofMinutes(30)),
    PT2H("PT2H", Duration.ofHours(2)),
    PT4H("PT4H", Duration.ofHours(4));

    @JsonValue
    private final String value;

    private final Duration duration;

    /**
     * Creates BucketSize from JSON value.
     */
    @JsonCreator
    public static BucketSize fromValue(final String value) {
        return Arrays.stream(values())
            .filter(size -> size.value.equals(value))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown BucketSize: " + value));
    }
}
