package io.github.eventify.api.admin.model.projection;

import java.time.LocalDate;

/**
 * Projection interface for daily event ingestion data from event_timeline_hourly.
 */
public interface DailyEventIngestion {

    /** Date of the aggregation bucket. */
    LocalDate getDate();

    /** Total events ingested on this date. */
    Long getEventCount();
}
