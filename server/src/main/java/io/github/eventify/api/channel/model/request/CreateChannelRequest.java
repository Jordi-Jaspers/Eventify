package io.github.eventify.api.channel.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Request DTO for creating a new channel.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Request to create a new channel")
public class CreateChannelRequest {

    @Schema(
        description = "Channel name",
        example = "My App Errors",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
        description = "Channel description",
        example = "Error logs from production",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;
}
