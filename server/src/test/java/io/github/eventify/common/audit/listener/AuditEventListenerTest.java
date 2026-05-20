package io.github.eventify.common.audit.listener;

import io.github.eventify.api.user.model.User;
import io.github.eventify.common.audit.event.AuditEvent;
import io.github.eventify.common.audit.model.AuditLog;
import io.github.eventify.common.audit.repository.AuditLogRepository;
import io.github.eventify.support.UnitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

@DisplayName("Unit Test - AuditEventListener")
public class AuditEventListenerTest extends UnitTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditEventListener listener;

    @BeforeEach
    public void setUp() {
        listener = new AuditEventListener(auditLogRepository);
    }

    @Test
    @DisplayName("Should persist AuditLog when valid AuditEvent received")
    public void shouldPersistAuditLogWhenValidAuditEventReceived() {
        // Given: A valid AuditEvent
        final AuditEvent event = anAuditEvent(1L, "GET", "/v1/admin/users", 200, null, "192.168.1.1");

        // When: Listener handles the event
        listener.onAuditEvent(event);

        // Then: AuditLog should be saved to repository
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should persist AuditLog with request body for POST event")
    public void shouldPersistAuditLogWithRequestBodyForPostEvent() {
        // Given: A POST AuditEvent with request body
        final String requestBody = "{\"email\":\"user@example.com\"}";
        final AuditEvent event = anAuditEvent(1L, "POST", "/v1/admin/users/1/role", 200, requestBody, "10.0.0.1");

        // When: Listener handles the event
        listener.onAuditEvent(event);

        // Then: AuditLog should be saved
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should swallow DB error and not propagate exception")
    public void shouldSwallowDbErrorAndNotPropagateException() {
        // Given: A valid AuditEvent but repository throws an exception
        final AuditEvent event = anAuditEvent(1L, "GET", "/v1/admin/users", 200, null, "192.168.1.1");
        doThrow(new RuntimeException("DB connection failed")).when(auditLogRepository).save(any(AuditLog.class));

        // When: Listener handles the event (should NOT throw)
        // Then: No exception propagated — original request flow is unaffected
        listener.onAuditEvent(event);

        // And: Save was attempted
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should persist AuditLog for 4xx failed request")
    public void shouldPersistAuditLogFor4xxFailedRequest() {
        // Given: A 403 AuditEvent
        final AuditEvent event = anAuditEvent(1L, "GET", "/v1/admin/users", 403, null, "192.168.1.1");

        // When: Listener handles the event
        listener.onAuditEvent(event);

        // Then: AuditLog should be saved even for failed requests
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("Should persist AuditLog for 5xx server error request")
    public void shouldPersistAuditLogFor5xxServerErrorRequest() {
        // Given: A 500 AuditEvent
        final AuditEvent event = anAuditEvent(1L, "POST", "/v1/admin/users/search", 500, null, "192.168.1.1");

        // When: Listener handles the event
        listener.onAuditEvent(event);

        // Then: AuditLog should be saved even for server errors
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    private static AuditEvent anAuditEvent(
        final Long actorId,
        final String method,
        final String path,
        final int statusCode,
        final String requestBody,
        final String ipAddress) {
        final User actor = new User();
        actor.setId(actorId);
        return new AuditEvent(actor, method, path, statusCode, requestBody, ipAddress);
    }
}
