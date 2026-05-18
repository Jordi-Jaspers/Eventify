package io.github.eventify.common.audit.event;

import io.github.eventify.api.user.model.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents an audit event published when an admin action occurs.
 */
@Getter
@RequiredArgsConstructor
public class AuditEvent {

    private final User actor;
    private final String method;
    private final String path;
    private final int statusCode;
    private final String requestBody;
    private final String ipAddress;
}
