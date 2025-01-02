package org.jordijaspers.eventify.api.authentication.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The request to login.
 */
@Data
@ToString
@NoArgsConstructor
public class LoginRequest {

    private String email;

    private String password;

}
