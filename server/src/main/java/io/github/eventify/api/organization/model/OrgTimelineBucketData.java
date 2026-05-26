package io.github.eventify.api.organization.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/** Domain model for a single timeline bucket. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class OrgTimelineBucketData {

    private LocalDateTime bucket;
    private long eventCount;

    /** Full constructor. */
    public OrgTimelineBucketData(final LocalDateTime bucket, final Long eventCount) {
        this.bucket = bucket;
        this.eventCount = eventCount == null ? 0L : eventCount;
    }
}
