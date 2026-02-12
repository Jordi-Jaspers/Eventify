package io.github.eventify.api.monitor.model.request;

import io.github.eventify.api.monitor.model.DurationDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Request for fetching duration details around a specific timestamp.
 */
@Data
@NoArgsConstructor
@Schema(description = "Duration details request")
public class DurationDetailsRequest {

    @Schema(
        description = "The timestamp to fetch durations around/before/after",
        example = "2026-02-12T10:20:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime timestamp;

    @Schema(
        description = "Direction for fetching durations relative to the timestamp",
        example = "AROUND",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private DurationDirection direction;
}
