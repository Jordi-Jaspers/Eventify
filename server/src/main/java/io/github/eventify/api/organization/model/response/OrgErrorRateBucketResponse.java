package io.github.eventify.api.organization.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/** Represents a single error rate bucket in the org error timeline. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "A single bucket in the org error rate timeline")
public class OrgErrorRateBucketResponse {

    @Schema(
        description = "Bucket timestamp",
        example = "2026-01-08T10:00:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime bucket;

    @Schema(
        description = "Error rate percentage (0-100) for this bucket",
        example = "12.5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private double errorRate;
}
