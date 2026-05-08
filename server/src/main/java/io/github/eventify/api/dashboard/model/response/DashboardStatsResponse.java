package io.github.eventify.api.dashboard.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

import static io.github.eventify.Main.SERIAL_VERSION_UID;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Response containing dashboard statistics.
 */
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Dashboard statistics response")
public class DashboardStatsResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    @Schema(
        description = "Number of events received in the last 24 hours",
        example = "150",
        requiredMode = REQUIRED
    )
    private long eventsToday;

    @Schema(
        description = "Number of active channels",
        example = "10",
        requiredMode = REQUIRED
    )
    private int activeChannels;

    @Schema(
        description = "Error rate percentage (0.0 - 100.0) based on channels with CRITICAL severity as last event",
        example = "20.0",
        requiredMode = REQUIRED
    )
    private double errorRate;

    @Schema(
        description = "Timestamp of the most recent event across all channels",
        example = "2026-02-11T10:30:00Z",
        requiredMode = NOT_REQUIRED
    )
    private OffsetDateTime lastEventAt;

    /**
     * Record-style constructor for immutable creation.
     *
     * @param eventsToday    events in last 24 hours
     * @param activeChannels count of active channels
     * @param errorRate      percentage of channels with critical errors
     * @param lastEventAt    timestamp of most recent event
     */
    public DashboardStatsResponse(
                                  final long eventsToday,
                                  final int activeChannels,
                                  final double errorRate,
                                  final OffsetDateTime lastEventAt
    ) {
        this.eventsToday = eventsToday;
        this.activeChannels = activeChannels;
        this.errorRate = errorRate;
        this.lastEventAt = lastEventAt;
    }

    /**
     * Record-style accessor for eventsToday.
     */
    public long eventsToday() {
        return eventsToday;
    }

    /**
     * Record-style accessor for activeChannels.
     */
    public int activeChannels() {
        return activeChannels;
    }

    /**
     * Record-style accessor for errorRate.
     */
    public double errorRate() {
        return errorRate;
    }

    /**
     * Record-style accessor for lastEventAt.
     */
    public OffsetDateTime lastEventAt() {
        return lastEventAt;
    }
}
