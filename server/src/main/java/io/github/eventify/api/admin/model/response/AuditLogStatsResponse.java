package io.github.eventify.api.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.List;

/** Response DTO containing audit log statistics for a given time range. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Audit log statistics for a given time range")
public class AuditLogStatsResponse {

    @Schema(
        description = "Total number of requests in the time range",
        example = "12847",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long totalRequests;

    @Schema(
        description = "Number of requests with status code >= 400",
        example = "296",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long errorCount;

    @Schema(
        description = "Number of mutating requests (POST, PUT, PATCH, DELETE)",
        example = "1523",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long mutationCount;

    @Schema(
        description = "Number of distinct actors in the time range",
        example = "14",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long uniqueActors;

    @Schema(
        description = "Hourly breakdown of requests and errors",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<HourlyBucket> hourlyBuckets;

    /** Hourly breakdown of request and error counts. */
    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    @Schema(description = "Hourly bucket of audit log activity")
    public static class HourlyBucket {

        @Schema(
            description = "Start of the hour bucket (UTC)",
            example = "2024-01-15T00:00:00Z",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private OffsetDateTime hour;

        @Schema(
            description = "Total requests in this hour",
            example = "120",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private Long total;

        @Schema(
            description = "Error requests (status >= 400) in this hour",
            example = "3",
            requiredMode = Schema.RequiredMode.REQUIRED
        )
        private Long errors;
    }
}
