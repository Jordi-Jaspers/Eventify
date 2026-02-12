package io.github.eventify.api.monitor.model;

import io.github.eventify.api.event.model.Severity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Represents a time duration with a specific severity state.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Timeline duration with severity and time range")
public class TimelineDuration implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Schema(
        description = "Severity level for this duration",
        example = "OK",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Severity severity;

    @Schema(
        description = "Start time of this duration",
        example = "2026-01-24T10:00:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime startTime;

    @Schema(
        description = "End time of this duration",
        example = "2026-01-24T11:00:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime endTime;

    /**
     * Creates a new TimelineDuration with the given parameters.
     *
     * @param severity  the severity level
     * @param startTime the start time
     * @param endTime   the end time (null for live/ongoing durations)
     * @return a new TimelineDuration instance
     */
    public static TimelineDuration of(
        final Severity severity,
        final OffsetDateTime startTime,
        final OffsetDateTime endTime
    ) {
        return new TimelineDuration()
            .setSeverity(severity)
            .setStartTime(startTime)
            .setEndTime(endTime);
    }
}
