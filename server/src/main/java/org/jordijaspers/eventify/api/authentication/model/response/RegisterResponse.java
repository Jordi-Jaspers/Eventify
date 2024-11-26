package org.jordijaspers.eventify.api.authentication.model.response;

import lombok.Data;


/**
 * A response model for user registration.
 */
@Data
public class RegisterResponse {

    private String email;

    private String authority;

    private boolean enabled;

    private boolean validated;

}
