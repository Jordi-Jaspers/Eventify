package org.jordijaspers.eventify.api.user.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * The password update request.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class PasswordRequest {

    private String newPassword;

    private String confirmPassword;

}
