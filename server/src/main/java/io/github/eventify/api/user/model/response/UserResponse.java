package io.github.eventify.api.user.model.response;

import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Response DTO for user search results.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class UserResponse implements PageableItemResource {

    @Schema(
        description = "Unique user identifier",
        example = "12345",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long id;

    @Schema(
        description = "User's email address",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;

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
