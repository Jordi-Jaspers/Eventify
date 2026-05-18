package io.github.eventify.api.admin.controller;

import io.github.eventify.api.admin.model.mapper.AuditLogMapper;
import io.github.eventify.api.admin.model.mapper.AuditLogStatsMapper;
import io.github.eventify.api.admin.model.response.AuditLogResponse;
import io.github.eventify.api.admin.model.response.AuditLogStatsData;
import io.github.eventify.api.admin.model.response.AuditLogStatsResponse;
import io.github.eventify.api.admin.service.AdminAuditLogService;
import io.github.eventify.common.audit.model.AuditLog;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.ADMIN_AUDIT_LOG_SEARCH_PATH;
import static io.github.eventify.api.Paths.ADMIN_AUDIT_LOG_STATS_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** REST controller for admin audit log endpoints. */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Audit Log (Admin)",
    description = "Administrative endpoints for searching and monitoring audit log entries"
)
public class AdminAuditLogController {

    private final AdminAuditLogService adminAuditLogService;

    private final AuditLogMapper auditLogMapper;

    private final AuditLogStatsMapper auditLogStatsMapper;

    /** Searches audit log entries with filtering and pagination. */
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Search audit log entries with filtering and pagination")
    @PostMapping(
        path = ADMIN_AUDIT_LOG_SEARCH_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageResource<AuditLogResponse>> searchAuditLog(@RequestBody final SortablePageInput input) {
        final Page<AuditLog> page = adminAuditLogService.searchAuditLog(input);
        return ResponseEntity.status(OK).body(auditLogMapper.toPageResource(page));
    }

    /** Returns audit log statistics for the given time range. */
    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Get audit log statistics for a time range")
    @GetMapping(
        path = ADMIN_AUDIT_LOG_STATS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuditLogStatsResponse> getAuditLogStats(
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime from,
        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime to
    ) {
        final AuditLogStatsData data = adminAuditLogService.getAuditLogStats(from, to);
        return ResponseEntity.status(OK).body(auditLogStatsMapper.toResponse(data.getStats(), data.getBuckets()));
    }
}
