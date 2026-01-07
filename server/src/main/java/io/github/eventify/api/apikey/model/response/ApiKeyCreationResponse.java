package io.github.eventify.api.apikey.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * Response DTO for creating an API key.
 * Contains the full key - only returned on creation.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ApiKeyCreationResponse {

    private Long id;
    private String name;
    private String key;
    private String suffix;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
}
