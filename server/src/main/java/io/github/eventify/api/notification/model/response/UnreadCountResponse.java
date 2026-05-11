package io.github.eventify.api.notification.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Response DTO for unread notification count.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Unread notification count")
public class UnreadCountResponse {

    @Schema(
        description = "Number of unread notifications",
        example = "3",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long count;
}
