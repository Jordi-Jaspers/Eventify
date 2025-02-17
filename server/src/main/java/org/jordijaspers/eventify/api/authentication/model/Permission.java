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
public enum Permission {

    ACCESS_APPLICATION("The permission to access the application."),
    ACCESS_EXTERNAL("The permission to access external API."),

    READ_DASHBOARDS("The permission to view all dashboards."),
    WRITE_DASHBOARDS("The permission to edit all dashboards."),

    READ_TEAMS("The permission to view all teams."),
    WRITE_TEAMS("The permission to edit all teams."),

    READ_USERS("The permission to view all users."),
    WRITE_USERS("The permission to edit all users."),

    READ_SOURCE("The permission to view all sources systems."),
    WRITE_SOURCE("The permission to edit all sources systems."),

    WRITE_EVENTS("The permission to send events to the Eventify API.");

    private final String description;

    /**
     * Retrieve all the configured permissions as a stream.
     *
     * @return A stream of the configured permissions.
     */
    public static Stream<Permission> stream() {
        return Stream.of(Permission.values());
    }

    /**
     * Retrieve all the configured permissions as a list.
     *
     * @return A list of all configured permissions.
     */
    public static List<Permission> getAll() {
        return Arrays.asList(Permission.values());
    }
}
