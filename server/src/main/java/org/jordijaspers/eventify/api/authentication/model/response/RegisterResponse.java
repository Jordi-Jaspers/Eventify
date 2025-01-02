package org.jordijaspers.eventify.api.authentication.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * A response model for user registration.
 */
@Data
@NoArgsConstructor
public class RegisterResponse {

    private String email;

    private String authority;

    private boolean enabled;

    private boolean validated;

}
