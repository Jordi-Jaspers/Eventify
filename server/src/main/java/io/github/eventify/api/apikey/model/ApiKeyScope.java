package io.github.eventify.api.apikey.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * Defines the scope of an API key.
 */
@Getter
@AllArgsConstructor
@Schema(description = "ApiKeyScope")
public enum ApiKeyScope {

    /**
     * API key scoped to a user.
     */
    USER("evt_"),

    /**
     * API key scoped to an organization.
     */
    ORGANIZATION("org_");

    private final String prefix;

    /**
     * Retrieve all the configured permissions as a stream.
     *
     * @return A stream of the configured scopes.
     */
    public Stream<ApiKeyScope> stream() {
        return Stream.of(values());
    }

}
