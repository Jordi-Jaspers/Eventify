package io.github.eventify.api.subscription.model.response;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Response DTO for a subscription.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Subscription details")
public class SubscriptionResponse {

    @Schema(
        description = "Unique subscription identifier",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
        description = "Watchlist ID this subscription belongs to",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long watchlistId;

    @Schema(
        description = "Target severities for notifications",
        example = "[\"CRITICAL\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<Severity> targetSeverities;

    @Schema(
        description = "Notification adapters",
        example = "[\"IN_APP\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<AdapterType> adapters;

    @Schema(
        description = "Subscription creation timestamp",
        example = "2026-01-08T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime createdAt;
}
