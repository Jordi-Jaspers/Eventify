package org.jordijaspers.eventify.api.authentication.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The request to register a user.
 */
@Data
@ToString
@NoArgsConstructor
public class RegisterUserRequest {

    private String email;

    private String firstName;

    private String lastName;

    private String password;

    private String passwordConfirmation;

}
