package io.github.eventify.api.user.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response for retention settings.
 */
@Data
@NoArgsConstructor
public class RetentionSettingsResponse {

    @Schema(
        description = "Number of days to retain data",
        example = "365",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer retentionDays;
}
