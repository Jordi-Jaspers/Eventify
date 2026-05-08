package io.github.eventify.api.user.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * The password update request.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class PasswordRequest {

    @Schema(
        description = "New password for the user",
        example = "********",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String newPassword;

    @Schema(
        description = "Password confirmation must match new password",
        example = "********",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String confirmPassword;

}
