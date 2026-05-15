package io.github.eventify.common.audit.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents an audit event published when an admin action occurs.
 */
@Getter
@RequiredArgsConstructor
public class AuditEvent {

    private final Long actorId;
    private final String method;
    private final String path;
    private final int statusCode;
    private final String requestBody;
    private final String ipAddress;
}
