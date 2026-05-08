package io.github.eventify.api.monitor.model.response;

import io.github.eventify.api.monitor.model.TimelineDuration;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Response containing duration details for a specific timestamp.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Duration details response with timeline durations and navigation metadata")
public class DurationDetailsResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Schema(
        description = "List of timeline durations",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<TimelineDuration> durations;

    @Schema(
        description = "Index of the selected duration in the list",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int selectedIndex;

    @Schema(
        description = "Whether there are previous durations available",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean hasPrevious;

    @Schema(
        description = "Whether there are next durations available",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean hasNext;
}
