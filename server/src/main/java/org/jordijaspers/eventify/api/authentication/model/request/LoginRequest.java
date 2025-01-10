package org.jordijaspers.eventify.api.authentication.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * The request to login.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class LoginRequest {

    private String email;

    private String password;

}
