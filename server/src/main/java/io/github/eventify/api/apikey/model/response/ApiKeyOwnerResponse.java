package io.github.eventify.api.apikey.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Owner information for an API key.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ApiKeyOwnerResponse {

    @Schema(
        description = "Owner ID",
        example = "456",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
        description = "Owner type (USER or ORGANIZATION)",
        example = "USER",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String type;

    @Schema(
        description = "Owner name",
        example = "John Doe",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @Schema(
        description = "Owner email (for user keys only)",
        example = "john@example.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String email;
}
