package org.jordijaspers.eventify.api.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The authority of a user.
 */
@Getter
@AllArgsConstructor
public enum Authority {

    NONE(
        "None",
        "The default authority for a user with no permissions.",
        Set.of()
    ),
    USER(
        "User",
        "The default authority for a user with limited permissions.",
        Set.of(
            Permission.ACCESS_APPLICATION,
            Permission.READ_DASHBOARDS,
            Permission.READ_TEAMS
        )
    ),
    MANAGER(
        "Manager",
        "The authority for a user which manages the dashboards and teams.",
        Set.of(
            Permission.ACCESS_APPLICATION,
            Permission.READ_DASHBOARDS,
            Permission.WRITE_DASHBOARDS,
            Permission.READ_TEAMS,
            Permission.WRITE_TEAMS,
            Permission.READ_USERS
        )
    ),
    ADMIN(
        "Admin",
        "The authority for an administrator with full permissions.",
        Set.of(
            Permission.ACCESS_APPLICATION,
            Permission.READ_DASHBOARDS,
            Permission.WRITE_DASHBOARDS,
            Permission.READ_TEAMS,
            Permission.WRITE_TEAMS,
            Permission.READ_USERS,
            Permission.WRITE_USERS
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
    public static Stream<Authority> stream() {
        return Stream.of(Authority.values());
    }

    /**
     * Retrieve all the configured permissions as a list.
     *
     * @return A list of all configured permissions.
     */
    public static List<Authority> getAll() {
        return Arrays.asList(Authority.values());
    }
}
