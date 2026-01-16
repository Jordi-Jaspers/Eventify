package io.github.eventify.api.user.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Request to update retention settings.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateRetentionRequest {

    @Schema(
        description = "Number of days to retain data (must be one of: 90, 180, 365, 730, 1095, 1825)",
        example = "365",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer retentionDays;
}
