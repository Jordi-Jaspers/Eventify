package io.github.eventify.api.admin.service;

import io.github.eventify.api.admin.model.AdminAuditLogMetaData;
import io.github.eventify.api.admin.model.response.AuditLogStatsData;
import io.github.eventify.api.admin.model.response.AuditLogStatsProjection;
import io.github.eventify.api.admin.model.response.HourlyBucketProjection;
import io.github.eventify.common.audit.model.AuditLog;
import io.github.eventify.common.audit.repository.AuditLogRepository;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for admin audit log search operations.
 */
@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

    private final AuditLogRepository auditLogRepository;

    private final AdminAuditLogMetaData adminAuditLogMetaData;

    /** Searches audit log entries using the given sortable page input. */
    @Transactional(readOnly = true)
    public Page<AuditLog> searchAuditLog(final SortablePageInput input) {
        final Sort sort = adminAuditLogMetaData.toSort(input.getSortOrder());
        final Pageable pageable = PageRequest.of(input.getPageNumber(), input.getPageSize(), sort);
        final Specification<AuditLog> spec = adminAuditLogMetaData.toSearchSpecification(input);
        return auditLogRepository.findAll(spec, pageable);
    }

    /** Returns audit log statistics and hourly buckets for the given time range. */
    @Transactional(readOnly = true)
    public AuditLogStatsData getAuditLogStats(final OffsetDateTime from, final OffsetDateTime to) {
        final AuditLogStatsProjection stats = auditLogRepository.findStatsBetween(from, to);
        final List<HourlyBucketProjection> buckets = auditLogRepository.findHourlyBucketsBetween(from, to);
        return new AuditLogStatsData(stats, buckets);
    }
}
