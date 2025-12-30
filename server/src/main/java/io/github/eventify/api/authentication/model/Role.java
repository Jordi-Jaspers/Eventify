package io.github.eventify.api.authentication.model;

import io.github.eventify.api.authentication.model.response.RoleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The authority of a user.
 */
@Getter
@AllArgsConstructor
public enum Role {

    USER(
        "User",
        "The default authority for a user with no permissions.",
        Set.of(
            Permission.ACCESS_APPLICATION
        )
    ),
    ADMIN(
        "Admin",
        "The authority for a user with all permissions.",
        Set.of(
            Permission.ACCESS_APPLICATION,
            Permission.MANAGE_USERS,
            Permission.PROVISION_ORGANIZATIONS,
            Permission.MANAGE_ORGANIZATIONS,
            Permission.VIEW_PLATFORM_STATS
        )
    );

    private final String name;

    private final String description;

    private final Set<Permission> permissions;

    /**
     * Retrieve all the configured permissions as a stream.
     *
     * @return A stream of the configured permissions.
     */
    public Stream<Role> stream() {
        return Stream.of(values());
    }

    /**
     * Retrieve all the configured permissions as a list.
     *
     * @return A list of all configured permissions.
     */
    public List<RoleResponse> getDisplayValues() {
        return stream()
            .map(role -> new RoleResponse(name(), role.getName(), role.getDescription()))
            .toList();
    }
}
