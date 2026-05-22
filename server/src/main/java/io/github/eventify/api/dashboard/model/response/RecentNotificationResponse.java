package io.github.eventify.api.dashboard.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/** Recent notification summary response. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Recent notification summary")
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class RecentNotificationResponse {

    @Schema(
        description = "Notification unique identifier",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
        description = "Notification title",
        example = "Channel Alert",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String title;

    @Schema(
        description = "Notification message body",
        example = "Channel X exceeded threshold",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String message;

    @Schema(
        description = "Notification category",
        example = "ALERT",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String category;

    @Schema(
        description = "Whether the notification is urgent",
        example = "false",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean urgent;

    @Schema(
        description = "Timestamp when the notification was created",
        example = "2026-01-08T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime createdAt;

    @Schema(
        description = "Optional action URL for the notification",
        example = "https://app.eventify.io/channels/1",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String actionUrl;

    /** Returns the notification title. */
    public String title() {
        return title;
    }
}
