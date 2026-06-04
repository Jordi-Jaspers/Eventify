package io.github.eventify.api.notification.adapter.model.request;

import io.github.eventify.api.notification.adapter.model.AdapterType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Request DTO for testing an adapter connection by type and webhook URL.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Request to test an adapter connection")
public class TestAdapterConnectionRequest {

    @Schema(
        description = "The type of adapter to test",
        example = "SLACK",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private AdapterType adapterType;

    @Schema(
        description = "The webhook URL to test",
        example = "https://your-webhook-url.example.com/webhook",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String webhookUrl;
}
