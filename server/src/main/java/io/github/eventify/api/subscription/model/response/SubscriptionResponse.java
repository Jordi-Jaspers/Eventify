package io.github.eventify.api.subscription.model.response;

import io.github.eventify.api.event.model.Severity;
import io.github.jframe.datasource.search.model.resource.PageableItemResource;
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
public class SubscriptionResponse implements PageableItemResource {

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
        description = "Watchlist name",
        example = "Production Servers",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String watchlistName;

    @Schema(
        description = "Organization ID for org-scoped subscriptions",
        example = "5",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long organizationId;

    @Schema(
        description = "Target severities for notifications",
        example = "[\"CRITICAL\"]",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<Severity> targetSeverities;

    @Schema(
        description = "Adapter config IDs for notification delivery",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<Long> adapterConfigIds;

    @Schema(
        description = "Subscription creation timestamp",
        example = "2026-01-08T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime createdAt;
}
