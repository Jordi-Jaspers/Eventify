package io.github.eventify.api.organization.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/** Response DTO for organization event timeline. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Organization event timeline")
public class OrgTimelineResponse {

    @Schema(
        description = "Hourly event timeline buckets",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<OrgTimelineBucketResponse> timeline;

    @Schema(
        description = "Error rate timeline buckets",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<OrgErrorRateBucketResponse> errorTimeline;
}
