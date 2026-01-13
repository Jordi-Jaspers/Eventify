package io.github.eventify.api.channel.model.response;

import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * Response DTO for channel details.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Channel details")
public class ChannelDetailsResponse implements PageableItemResource {

    @Schema(
        description = "Unique channel identifier",
        example = "123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

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

    @Schema(
        description = "Channel status",
        example = "ACTIVE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String status;

    @Schema(
        description = "Timestamp when the channel was created",
        example = "2026-01-08T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime createdAt;

    @Schema(
        description = "Timestamp when the channel was last updated",
        example = "2026-01-08T15:45:00Z",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OffsetDateTime updatedAt;
}
