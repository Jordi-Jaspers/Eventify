package io.github.eventify.api.organization.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/** Domain model for a single error rate bucket. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class OrgErrorRateBucket {

    private LocalDateTime bucket;
    private double errorRate;

    /** Full constructor. */
    public OrgErrorRateBucket(final LocalDateTime bucket, final Double errorRate) {
        this.bucket = bucket;
        this.errorRate = errorRate == null ? 0.0 : errorRate;
    }
}
