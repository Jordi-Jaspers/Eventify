package io.github.eventify.api.user.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(callSuper = true)
public class UpdatePasswordRequest extends PasswordRequest {

    @Schema(
        description = "User's current password for verification",
        example = "********",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String oldPassword;

}
