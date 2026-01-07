package io.github.eventify.api.apikey.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating an API key.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class CreateApiKeyRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    private OffsetDateTime expiresAt;
}
