package io.github.eventify.api.notification.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Response DTO for mark-all-read operation.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Result of marking all notifications as read")
public class MarkAllReadResponse {

    @Schema(
        description = "Number of notifications marked as read",
        example = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int markedCount;
}
