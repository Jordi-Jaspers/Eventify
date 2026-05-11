package io.github.eventify.api.notification.model.response;

import io.github.eventify.api.notification.model.NotificationCategory;
import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * Response DTO for a single notification.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Notification details")
public class NotificationResponse implements PageableItemResource {

    @Schema(
        description = "Unique notification identifier",
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
        description = "Notification title",
        example = "Welcome to Eventify",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String title;

    @Schema(
        description = "Notification message",
        example = "Get started by creating your first channel",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String message;

    @Schema(
        description = "Optional action URL",
        example = "/channels",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String actionUrl;

    @Schema(
        description = "Optional action label",
        example = "Get started",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String actionLabel;

    @Schema(
        description = "Whether the notification is urgent",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean urgent;

    @Schema(
        description = "Timestamp when the notification was read",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OffsetDateTime readAt;

    @Schema(
        description = "Timestamp when the notification was created",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime createdAt;
}
