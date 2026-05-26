package io.github.eventify.api.organization.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/** Domain model for organization event statistics. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class OrgStats {

    private List<OrgTimelineBucketData> timeline;
    private long totalEvents;
    private long totalChannels;
    private List<OrgTimelineBucketData> errorTimeline;
    private long avgDailyVolume;
    private double currentErrorRate;
    private OrgApiKeyStatistics apiKeyStats;
}
