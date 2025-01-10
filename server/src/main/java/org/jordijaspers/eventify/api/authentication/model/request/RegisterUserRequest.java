package org.jordijaspers.eventify.api.authentication.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * The request to register a user.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class RegisterUserRequest {

    private String email;

    private String firstName;

    private String lastName;

    private String password;

    private String passwordConfirmation;

}
