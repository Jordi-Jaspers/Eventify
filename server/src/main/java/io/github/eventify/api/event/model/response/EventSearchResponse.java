package io.github.eventify.api.event.model.response;

import io.github.eventify.api.event.model.Severity;
import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * Response DTO for event search results.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Event search result")
public class EventSearchResponse implements PageableItemResource {

    @Schema(
        description = "Event message",
        example = "Application started successfully",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String message;

    @Schema(
        description = "Event timestamp",
        example = "2026-01-30T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime timestamp;

    @Schema(
        description = "Event severity level",
        example = "OK",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Severity severity;
}
