package io.github.eventify.api.notification.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Response DTO for a broadcast preview (recipient count only).
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Broadcast preview with recipient count")
public class PreviewResponse {

    @Schema(
        description = "Number of recipients for the given audience",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int recipientCount;
}
