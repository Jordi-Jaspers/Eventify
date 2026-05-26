package io.github.eventify.api.organization.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/** Domain model for organization event summary statistics. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class OrgSummary {

    private long totalEvents;
    private long avgDailyVolume;
    private double currentErrorRate;
    private long totalChannels;
}
