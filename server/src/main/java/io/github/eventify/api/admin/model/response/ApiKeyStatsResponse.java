package io.github.eventify.api.admin.model.response;

import io.github.eventify.api.apikey.model.response.ApiKeyResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * Response object containing admin API key statistics.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ApiKeyStatsResponse {

    @Schema(
        description = "Total number of active API keys",
        example = "150",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long totalKeys;

    @Schema(
        description = "Total number of user-scoped API keys",
        example = "100",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long userKeys;

    @Schema(
        description = "Total number of organization-scoped API keys",
        example = "50",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long organizationKeys;

    @Schema(
        description = "Number of keys created in the last 7 days",
        example = "15",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long createdThisWeek;

    @Schema(
        description = "Number of keys created in the last 30 days",
        example = "42",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long createdThisMonth;

    @Schema(
        description = "Number of keys revoked in the last 30 days",
        example = "8",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long revokedThisMonth;

    @Schema(
        description = "Number of keys expiring in the next 30 days",
        example = "12",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long expiringNext30Days;

    @Schema(
        description = "Number of keys that have never been used",
        example = "25",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long neverUsedKeys;

    @Schema(
        description = "Top 5 API keys by request volume",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<ApiKeyResponse> topKeysByUsage;
}
