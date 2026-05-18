package io.github.eventify.api.admin.model.response;

import io.github.jframe.datasource.search.model.resource.PageableItemResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/** Response DTO representing a single audit log entry. */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Audit log entry details")
public class AuditLogResponse implements PageableItemResource {

    @Schema(
        description = "Unique audit log entry identifier",
        example = "123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
        description = "Email of the actor who performed the action",
        example = "admin@example.com",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String actorEmail;

    @Schema(
        description = "HTTP method",
        example = "GET",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String method;

    @Schema(
        description = "Request path",
        example = "/v1/channels",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String path;

    @Schema(
        description = "HTTP status code",
        example = "200",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Short statusCode;

    @Schema(
        description = "Client IP address",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String ipAddress;

    @Schema(
        description = "Timestamp of the request",
        example = "2026-01-08T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private OffsetDateTime createdAt;

    @Schema(
        description = "Request body (JSON)",
        example = "{\"name\":\"test\"}",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String requestBody;
}
