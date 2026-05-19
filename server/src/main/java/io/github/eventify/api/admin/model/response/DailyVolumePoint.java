package io.github.eventify.api.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/** A single day's event volume data point. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Schema(description = "Daily event volume data point")
public class DailyVolumePoint {

    @Schema(
        description = "Date of the data point",
        example = "2026-01-15",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private LocalDate date;

    @Schema(
        description = "Number of events on this date",
        example = "1500",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long eventCount;
}
