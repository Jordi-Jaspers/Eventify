package io.github.eventify.api.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Severity breakdown of events in the period.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "Event severity breakdown")
public class SeverityBreakdownResponse {

    @Schema(
        description = "Number of critical events",
        example = "50",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long critical;

    @Schema(
        description = "Number of warning events",
        example = "120",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long warning;

    @Schema(
        description = "Number of ok events",
        example = "830",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long ok;
}
