package io.github.eventify.api.admin.stats.model.projection;

import java.time.LocalDateTime;

/** Projection interface for org timeline data from event_timeline_hourly. */
public interface OrgTimelineProjection {

    /** Returns the bucket timestamp. */
    LocalDateTime getBucket();

    /** Returns the event count for this bucket. */
    Long getEventCount();
}
