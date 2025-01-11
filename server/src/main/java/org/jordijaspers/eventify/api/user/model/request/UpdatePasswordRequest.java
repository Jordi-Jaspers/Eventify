package org.jordijaspers.eventify.api.user.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(callSuper = true)
public class UpdatePasswordRequest extends PasswordRequest {

    private String oldPassword;

}
