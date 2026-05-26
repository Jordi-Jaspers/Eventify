package io.github.eventify.api.organization.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/** Response DTO for organization event summary statistics. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Organization event summary statistics")
public class OrgSummaryResponse {

    @Schema(
        description = "Total number of events in the time window",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long totalEvents;

    @Schema(
        description = "Average daily event volume",
        example = "14",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long avgDailyVolume;

    @Schema(
        description = "Current error rate as percentage",
        example = "5.0",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private double currentErrorRate;

    @Schema(
        description = "Total number of channels in the organization",
        example = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long totalChannels;
}
