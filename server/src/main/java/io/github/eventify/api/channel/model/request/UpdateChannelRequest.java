package io.github.eventify.api.channel.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Request DTO for updating a channel.
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Request to update a channel")
public class UpdateChannelRequest {

    @Schema(
        description = "Channel name",
        example = "Updated Channel Name",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
        description = "Channel description",
        example = "Updated description",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;
}
