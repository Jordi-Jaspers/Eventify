package io.github.eventify.api.organization.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/** API key statistics for an organization. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "API key statistics for an organization")
public class OrgApiKeyStatsResponse {

    @Schema(
        description = "Number of API keys revoked this month",
        example = "2",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long revokedThisMonth;

    @Schema(
        description = "Number of API keys expiring this month",
        example = "3",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long expiringThisMonth;

    @Schema(
        description = "Number of API keys that have never been used",
        example = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long neverUsed;

    @Schema(
        description = "Top API keys by request volume",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<OrgTopApiKeyResponse> topKeys;
}
