package io.github.eventify.api.apikey.repository;

import io.github.eventify.api.apikey.model.ApiKeyAudit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link ApiKeyAudit} entities.
 */
@Repository
public interface ApiKeyAuditRepository extends JpaRepository<ApiKeyAudit, Long> {
}
