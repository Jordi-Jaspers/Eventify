package io.github.eventify.api.user.model.request;

import io.github.eventify.api.authentication.model.Role;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * The user details update request.
 */
@Data
@ToString
@NoArgsConstructor
@Accessors(chain = true)
public class UpdateRoleRequest {

    private Role role;

}
