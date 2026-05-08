package io.github.eventify.api.authentication.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * A response model for user registration.
 */
@Data
@NoArgsConstructor
public class RegisterResponse {

    @Schema(
        description = "User's email address",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;

    @Schema(
        description = "User's authority/role",
        example = "USER",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String authority;

    @Schema(
        description = "Whether the user account is enabled",
        example = "true",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private boolean enabled;

    @Schema(
        description = "Whether the user's email has been validated",
        example = "false",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private boolean validated;

}
