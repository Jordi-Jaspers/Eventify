package io.github.eventify.api.notification.adapter.model.response;

import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Response DTO for adapter configuration resources.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Adapter configuration resource")
public class AdapterConfigResponse {

    @Schema(
        description = "Unique identifier",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
        description = "Adapter type identifier",
        example = "IN_APP",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private AdapterType adapterType;

    @Schema(
        description = "Human-readable label",
        example = "My In-App Notifier",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String label;

    @Schema(
        description = "Configuration properties (webhookUrl masked)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Map<String, Object> config;

    @Schema(
        description = "Whether this configuration is enabled",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean enabled;

    @Schema(
        description = "Whether this config is system-managed (cannot be deleted by user)",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean systemManaged;

    @Schema(
        description = "Creation timestamp",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OffsetDateTime createdAt;

    @Schema(
        description = "Last update timestamp",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OffsetDateTime updatedAt;
}
