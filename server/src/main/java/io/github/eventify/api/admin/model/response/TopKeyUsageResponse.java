package io.github.eventify.api.admin.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Response object for top API key usage data.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TopKeyUsageResponse {

    @Schema(
        description = "API key ID",
        example = "123",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long id;

    @Schema(
        description = "API key prefix (scope + first 8 chars)",
        example = "evt_abcdefgh",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String prefix;

    @Schema(
        description = "API key name",
        example = "Production Server",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String name;

    @Schema(
        description = "Owner name (user or organization)",
        example = "John Doe",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String ownerName;

    @Schema(
        description = "Total number of requests",
        example = "10000",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long totalRequests;
}
