package org.jordijaspers.eventify.api.authentication.model.request;

import lombok.Data;
import lombok.ToString;

/**
 * The request to refresh the token.
 */
@Data
@ToString
public class RefreshTokenRequest {

    private String refreshToken;

}
