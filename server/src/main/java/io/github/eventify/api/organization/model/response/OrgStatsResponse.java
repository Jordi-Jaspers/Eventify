package io.github.eventify.api.organization.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/** Response DTO for org event statistics. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Organization event statistics")
public class OrgStatsResponse {

    @Schema(
        description = "Hourly event timeline buckets",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<OrgTimelineBucketResponse> timeline;

    @Schema(
        description = "Total number of events in the time window",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long totalEvents;

    @Schema(
        description = "Total number of channels in the organization",
        example = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long totalChannels;

    @Schema(description = "Error event timeline buckets")
    private List<OrgTimelineBucketResponse> errorTimeline;

    @Schema(
        description = "Average daily event volume",
        example = "14"
    )
    private long avgDailyVolume;

    @Schema(
        description = "Current error rate as percentage",
        example = "5.0"
    )
    private double currentErrorRate;

    @Schema(description = "API key statistics")
    private OrgApiKeyStatsResponse apiKeyStats;

}
