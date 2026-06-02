package io.github.eventify.api.organization.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/** Domain model for organization event timeline. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class OrgTimeline {

    private List<OrgTimelineBucketData> timeline;
    private List<OrgErrorRateBucket> errorTimeline;
}
