package io.github.eventify.api.session.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/** Domain class representing a user session. */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionInfo {

    private Long id;
    private String deviceInfo;
    private String ipAddress;
    private String userAgent;
    private OffsetDateTime lastActiveAt;
    private OffsetDateTime createdAt;
    private boolean current;
    private OffsetDateTime expiresAt;
}
