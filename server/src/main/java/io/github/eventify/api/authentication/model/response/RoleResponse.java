package io.github.eventify.api.authentication.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The authority of a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {

    @Schema(
        description = "Role identifier key",
        example = "USER",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String key;

    @Schema(
        description = "Human-readable role name",
        example = "User",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String value;

    @Schema(
        description = "Description of the role and its permissions",
        example = "The default authority for a user with no permissions.",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;

}
