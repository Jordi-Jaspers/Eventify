package io.github.eventify.api.subscription.model.request;

import io.github.eventify.api.event.model.Severity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Request DTO for creating a subscription.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Create subscription request")
public class CreateSubscriptionRequest {

    @Schema(
        description = "Watchlist ID to subscribe to",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long watchlistId;

    @Schema(
        description = "Severity levels to receive notifications for",
        example = "[\"CRITICAL\", \"WARNING\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<Severity> targetSeverities;

    @Schema(
        description = "Adapter config IDs to use for notification delivery",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<Long> adapterConfigIds;
}
