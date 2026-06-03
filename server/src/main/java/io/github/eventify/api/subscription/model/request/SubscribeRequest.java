package io.github.eventify.api.subscription.model.request;

import io.github.eventify.api.event.model.Severity;
import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request DTO for subscribing to a watchlist.
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Subscribe request for a watchlist")
public class SubscribeRequest {

    @Schema(
        description = "Severity levels to receive notifications for",
        example = "[\"CRITICAL\", \"WARNING\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<Severity> targetSeverities;

    @Schema(
        description = "Notification adapters to use for delivery",
        example = "[\"IN_APP\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<AdapterType> adapters;
}
