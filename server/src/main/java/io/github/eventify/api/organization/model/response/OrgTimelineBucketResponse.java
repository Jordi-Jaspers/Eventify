package io.github.eventify.api.organization.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/** Represents a single hourly bucket in the org event timeline. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "A single hourly bucket in the org event timeline")
public class OrgTimelineBucketResponse {

    @Schema(
        description = "Bucket timestamp (hourly)",
        example = "2026-01-08T10:00:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime bucket;

    @Schema(
        description = "Number of events in this bucket",
        example = "15",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long eventCount;

    /** Full constructor. */
    public OrgTimelineBucketResponse(final LocalDateTime bucket, final Long eventCount) {
        this.bucket = bucket;
        this.eventCount = eventCount == null ? 0L : eventCount;
    }
}
