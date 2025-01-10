package org.jordijaspers.eventify.api.authentication.model.request;

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

    private String refreshToken;

}
