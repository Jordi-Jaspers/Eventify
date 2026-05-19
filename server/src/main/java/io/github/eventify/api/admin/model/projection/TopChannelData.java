package io.github.eventify.api.admin.model.projection;

/**
 * Projection interface for top channel data from event_timeline_hourly.
 */
public interface TopChannelData {

    /** Channel identifier. */
    Long getChannelId();

    /** Channel name. */
    String getChannelName();

    /** Owner name (organization or user email). */
    String getOwnerName();

    /** Total event count in period. */
    Long getEventCount();

    /** Percentage of total events. */
    Double getPercentage();
}
