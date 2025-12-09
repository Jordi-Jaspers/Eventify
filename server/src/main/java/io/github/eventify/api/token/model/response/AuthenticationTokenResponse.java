package io.github.eventify.api.token.model.response;

import io.github.eventify.api.token.model.Token;
import lombok.Data;


/**
 * The response object for the authentication token.
 */
@Data
public class AuthenticationTokenResponse {

    private String accessToken;

    private Token refreshToken;

    private int expiresAt;

}
