package io.github.eventify.api.session.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;

/**
 * Response DTO representing a user session (refresh token).
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "User session information")
public class SessionResponse {

    @Schema(
        description = "Unique session identifier",
        example = "42",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long id;

    @Schema(
        description = "Human-readable device label",
        example = "Chrome on macOS",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String deviceInfo;

    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    @Schema(
        description = "IP address of the session",
        example = "203.0.113.0",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String ipAddress;

    @Schema(
        description = "Raw user-agent string",
        example = "Mozilla/5.0 ...",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String userAgent;

    @Schema(
        description = "Last activity timestamp",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OffsetDateTime lastActiveAt;

    @Schema(
        description = "Session creation timestamp",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OffsetDateTime createdAt;

    @Schema(
        description = "Whether this session matches the current request's refresh-token cookie",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean current;
}
