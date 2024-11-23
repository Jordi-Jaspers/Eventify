package org.jordijaspers.eventify.api.authentication.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * The authority of a user.
 */
@Getter
@AllArgsConstructor
public enum Authority {

    USER("The default authority for a user with limited permissions."),
    MANAGER("The authority for a user which manages the dashboards and teams."),
    ADMIN("The authority for a user with administrative permissions.");

    /**
     * The reason.
     */
    private final String description;

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
