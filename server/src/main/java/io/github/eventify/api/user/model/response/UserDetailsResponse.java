package io.github.eventify.api.user.model.response;

import io.github.eventify.api.authentication.model.Permission;
import io.github.eventify.api.authentication.model.Role;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * A response model for user details.
 */
@Data
@NoArgsConstructor
public class UserDetailsResponse {

    private Long id;

    private String email;

    private String firstName;

    private String lastName;

    private ZonedDateTime lastLogin;

    private ZonedDateTime created;

    private Role role;

    private Set<Permission> permissions = EnumSet.noneOf(Permission.class);

    private boolean enabled;

    private boolean validated;

}
