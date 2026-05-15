package io.github.eventify.common.audit.listener;

import io.github.eventify.common.audit.event.AuditEvent;
import io.github.eventify.common.audit.model.AuditLog;
import io.github.eventify.common.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static io.github.eventify.common.util.TimeProvider.now;

/**
 * Async listener that persists AuditEvent to the audit_log table.
 * Swallows DB errors to avoid disrupting the original request flow.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    /**
     * Persists audit events asynchronously. Failures are logged but never propagated.
     */
    @Async
    @EventListener
    @Transactional
    public void onAuditEvent(final AuditEvent event) {
        try {
            auditLogRepository.save(
                new AuditLog(
                    event.getActorId(),
                    event.getMethod(),
                    event.getPath(),
                    (short) event.getStatusCode(),
                    event.getRequestBody(),
                    event.getIpAddress(),
                    now()
                )
            );
        } catch (final Exception e) {
            log.warn("Failed to persist audit log: {}", e.getMessage());
        }
    }
}
