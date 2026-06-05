package io.github.eventify.api.subscription.model.request;

import io.github.eventify.api.event.model.Severity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Request DTO for updating a subscription.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Update subscription request")
public class UpdateSubscriptionRequest {

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
