package io.github.eventify.api.user.model.request;

import io.github.eventify.api.authentication.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * The user details update request.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateRoleRequest {

    @Schema(
        description = "New role to assign to the user",
        example = "USER",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Role role;

}
