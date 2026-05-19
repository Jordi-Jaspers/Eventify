package io.github.eventify.api.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/** Domain class representing dashboard statistics. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStats {

    private long eventsToday;
    private int activeChannels;
    private double errorRate;
    private OffsetDateTime lastEventAt;
}
