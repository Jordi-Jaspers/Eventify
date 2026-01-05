package io.github.eventify.api.admin.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Request for assigning an owner to an organization.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class AssignOwnerRequest {

    @Schema(
        description = "Email address of the user to assign as owner",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;

    @Schema(
        description = "User ID to assign as owner",
        example = "12345",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long userId;
}
