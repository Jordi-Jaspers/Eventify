package io.github.eventify.api.admin.stats.model.projection;

import java.time.LocalDate;

/**
 * Projection interface for daily event stats from admin_event_stats_daily continuous aggregate.
 */
public interface DailyEventStats {

    /** Date of the aggregation bucket. */
    LocalDate getDay();

    /** Total events for this day. */
    Long getTotalEvents();
}
