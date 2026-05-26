package io.github.eventify.api.organization.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/** Top API key by request volume. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Top API key by request volume")
public class OrgTopApiKeyResponse {

    @Schema(
        description = "API key name",
        example = "Production Key",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
        description = "API key suffix",
        example = "abc123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String suffix;

    @Schema(
        description = "Total number of requests made with this key",
        example = "1500",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long totalRequests;
}
