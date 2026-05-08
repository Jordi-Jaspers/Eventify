package io.github.eventify.api.authentication.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * The request to login.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class LoginRequest {

    @Schema(
        description = "User's email address for authentication",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Schema(
        description = "User's password",
        example = "********",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    @Schema(
        description = "Whether to extend the session lifetime (30 days instead of 7). Defaults to false.",
        example = "false",
        defaultValue = "false"
    )
    private boolean rememberMe;

}
