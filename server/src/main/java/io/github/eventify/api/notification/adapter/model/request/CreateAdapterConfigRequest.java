package io.github.eventify.api.notification.adapter.model.request;

import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * Request DTO for creating an adapter configuration.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Request to create an adapter configuration")
public class CreateAdapterConfigRequest {

    @Schema(
        description = "Adapter type identifier",
        example = "IN_APP",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private AdapterType adapterType;

    @Schema(
        description = "Human-readable label for this configuration",
        example = "My In-App Notifier",
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
