package org.jordijaspers.eventify.api.token.model.response;

import lombok.Data;

import org.jordijaspers.eventify.api.token.model.Token;

/**
 * The response object for the authentication token.
 */
@Data
public class AuthenticationTokenResponse {

    private String accessToken;

    private Token refreshToken;

    private int expiresAt;

}
