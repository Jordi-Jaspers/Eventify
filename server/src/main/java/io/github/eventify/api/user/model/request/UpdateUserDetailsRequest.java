package io.github.eventify.api.user.model.request;

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
public class UpdateUserDetailsRequest {

    @Schema(
        description = "User's first name",
        example = "John",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String firstName;

    @Schema(
        description = "User's last name",
        example = "Doe",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String lastName;

}
