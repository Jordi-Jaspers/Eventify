package io.github.eventify.api.monitor.model;

import io.github.eventify.api.monitor.model.response.TimelineDuration;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * A timeline representing a series of severity durations over time.
 * Reusable for channels, dashboards, or any time-based severity visualization.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Timeline with severity durations")
public class Timeline implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Schema(
        description = "List of severity durations",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<TimelineDuration> durations;

    /**
     * Creates an empty timeline with no segments.
     *
     * @return empty Timeline
     */
    public static Timeline empty() {
        return new Timeline(List.of());
    }
}
