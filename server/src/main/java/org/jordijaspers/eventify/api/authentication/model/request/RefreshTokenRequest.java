package org.jordijaspers.eventify.api.authentication.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The request to refresh the token.
 */
@Data
@ToString
@NoArgsConstructor
public class RefreshTokenRequest {

    private String refreshToken;

}
