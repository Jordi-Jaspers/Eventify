package io.github.eventify.api.notification.adapter.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Request DTO for updating an adapter configuration.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Request to update an adapter configuration")
public class UpdateAdapterConfigRequest {

    @Schema(
        description = "Human-readable label for this configuration",
        example = "Updated Label",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String label;

    @Schema(
        description = "Adapter-specific configuration properties",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Map<String, Object> config;

    @Schema(
        description = "Whether this configuration is enabled",
        example = "true",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private boolean enabled = true;
}
