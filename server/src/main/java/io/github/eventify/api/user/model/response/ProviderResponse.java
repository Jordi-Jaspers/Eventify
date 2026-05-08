package io.github.eventify.api.user.model.response;

import io.github.eventify.api.user.model.AuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Response DTO representing a connected authentication provider.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Authentication provider connection status")
public class ProviderResponse {

    @Schema(
        description = "Unique identifier of the linked provider record (null when not connected)",
        example = "42",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long id;

    @Schema(
        description = "The authentication provider type",
        example = "GOOGLE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private AuthProvider provider;

    @Schema(
        description = "Whether this provider is connected to the user account",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean connected;

    @Schema(
        description = "Email address associated with this provider (null when not connected)",
        example = "user@gmail.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String providerEmail;
}
