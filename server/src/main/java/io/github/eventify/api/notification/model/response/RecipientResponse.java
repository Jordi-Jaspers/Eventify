package io.github.eventify.api.notification.model.response;

import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Response DTO representing a single broadcast recipient.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Broadcast recipient details")
public class RecipientResponse implements PageableItemResource {

    @Schema(
        description = "User ID of the recipient",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long userId;

    @Schema(
        description = "Email address of the recipient",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Schema(
        description = "Full name of the recipient",
        example = "John Doe",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String name;
}
