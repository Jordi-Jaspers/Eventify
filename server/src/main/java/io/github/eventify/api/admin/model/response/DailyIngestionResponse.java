package io.github.eventify.api.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * A single data point in the daily event ingestion series.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "Daily event ingestion data point")
public class DailyIngestionResponse {

    @Schema(
        description = "Date of the data point",
        example = "2026-04-20",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate date;

    @Schema(
        description = "Total events ingested on this day",
        example = "1500",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long eventCount;
}
