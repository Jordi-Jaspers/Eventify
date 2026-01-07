package io.github.eventify.api.apikey.model.response;

import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * Response DTO for API key in list view.
 * Contains masked key - never includes full key.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ApiKeyResponse implements PageableItemResource {

    private Long id;

    private String name;

    private String maskedKey;

    private OffsetDateTime createdAt;

    private OffsetDateTime expiresAt;

    private OffsetDateTime lastUsedAt;

    private Long totalRequests;
}
