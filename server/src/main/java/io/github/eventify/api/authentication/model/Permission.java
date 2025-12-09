package io.github.eventify.api.authentication.model;

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
public enum Permission {

    ACCESS_APPLICATION("The permission to access the application."),
    MANAGE_USERS("The permission to manage the user.");

    private final String description;

    /**
     * Retrieve all the configured permissions as a stream.
     *
     * @return A stream of the configured permissions.
     */
    public static Stream<Permission> stream() {
        return Stream.of(values());
    }

    /**
     * Retrieve all the configured permissions as a list.
     *
     * @return A list of all configured permissions.
     */
    public static List<Permission> getAll() {
        return Arrays.asList(values());
    }
}
