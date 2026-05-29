package io.github.eventify.api.admin.stats.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/** Request body for admin stats endpoints. Exactly one mode must be set: days OR (startDate + endDate). */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Admin stats request — use either days or startDate+endDate, not both")
public class AdminStatsRequest {

    @Schema(
        description = "Number of days to look back (1–365)",
        example = "30",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Integer days;

    @Schema(
        description = "Start date of explicit range (inclusive)",
        example = "2024-01-01",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private LocalDate startDate;

    @Schema(
        description = "End date of explicit range (inclusive, must not be in future)",
        example = "2024-01-31",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private LocalDate endDate;
}
