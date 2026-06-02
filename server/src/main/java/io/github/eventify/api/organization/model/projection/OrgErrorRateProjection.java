package io.github.eventify.api.organization.model.projection;

import java.time.LocalDateTime;

/** Projection interface for org error rate timeline data. */
public interface OrgErrorRateProjection {

    /** Returns the bucket timestamp. */
    LocalDateTime getBucket();

    /** Returns the error rate percentage (0-100) for this bucket. */
    Double getErrorRate();
}
