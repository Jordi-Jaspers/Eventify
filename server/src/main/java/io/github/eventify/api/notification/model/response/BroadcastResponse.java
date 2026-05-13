package io.github.eventify.api.notification.model.response;

import io.github.eventify.api.notification.model.NotificationAudienceType;
import io.github.eventify.api.notification.model.NotificationCategory;
import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * Response DTO for a notification broadcast.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Notification broadcast details")
public class BroadcastResponse implements PageableItemResource {

    @Schema(
        description = "Unique broadcast identifier",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
        description = "Notification category",
        example = "ANNOUNCEMENT",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private NotificationCategory category;

    @Schema(
        description = "Broadcast title",
        example = "System Update",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String title;

    @Schema(
        description = "Broadcast message",
        example = "We will be down for maintenance.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String message;

    @Schema(
        description = "Action URL",
        example = "/dashboard",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String actionUrl;

    @Schema(
        description = "Action label",
        example = "Go to Dashboard",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String actionLabel;

    @Schema(
        description = "Audience type",
        example = "ALL_USERS",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private NotificationAudienceType audienceType;

    @Schema(
        description = "Number of recipients",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int recipientCount;

    @Schema(
        description = "Broadcast creation timestamp",
        example = "2026-01-08T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime createdAt;

    @Schema(
        description = "Email of the admin who sent the broadcast",
        example = "admin@example.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String sentByEmail;
}
