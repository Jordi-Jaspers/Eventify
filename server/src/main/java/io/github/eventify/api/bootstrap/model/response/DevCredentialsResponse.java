package io.github.eventify.api.bootstrap.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Response containing development credentials.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class DevCredentialsResponse {

    @Schema(
        description = "Email address for development authentication",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;

    @Schema(
        description = "Password for development authentication",
        example = "********",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String password;

}
