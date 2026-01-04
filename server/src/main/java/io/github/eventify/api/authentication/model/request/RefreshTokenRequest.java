package io.github.eventify.api.authentication.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * The request to refresh the token.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class RefreshTokenRequest {

    @Schema(
        description = "JWT refresh token used to obtain a new access token",
        example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;

}
