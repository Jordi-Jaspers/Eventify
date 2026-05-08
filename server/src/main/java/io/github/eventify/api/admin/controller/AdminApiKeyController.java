package io.github.eventify.api.admin.controller;

import io.github.eventify.api.admin.model.mapper.ApiKeyAuditMapper;
import io.github.eventify.api.admin.model.response.ApiKeyAuditResponse;
import io.github.eventify.api.admin.model.response.ApiKeyStatsResponse;
import io.github.eventify.api.admin.service.AdminApiKeyService;
import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.ApiKeyAudit;
import io.github.eventify.api.apikey.model.mapper.ApiKeyMapper;
import io.github.eventify.api.apikey.model.response.ApiKeyResponse;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.*;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Admin controller for managing all API keys across the system.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "API Key Management (Admin)",
    description = "Administrative endpoints for managing and monitoring all API keys"
)
public class AdminApiKeyController {

    private final AdminApiKeyService adminApiKeyService;

    private final ApiKeyMapper apiKeyMapper;

    private final ApiKeyAuditMapper apiKeyAuditMapper;

    @ResponseStatus(OK)
    @PreAuthorize("hasAnyAuthority('MANAGE_USERS', 'MANAGE_ORGANIZATIONS')")
    @Operation(summary = "Get API key statistics")
    @GetMapping(
        path = ADMIN_API_KEYS_STATS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiKeyStatsResponse> getStatistics() {
        final ApiKeyStatsResponse stats = adminApiKeyService.getStatistics();
        return ResponseEntity.status(OK).body(stats);
    }

    @ResponseStatus(OK)
    @PreAuthorize("hasAnyAuthority('MANAGE_USERS', 'MANAGE_ORGANIZATIONS')")
    @Operation(summary = "Search all API keys with filtering and pagination")
    @PostMapping(
        path = ADMIN_API_KEYS_SEARCH_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageResource<ApiKeyResponse>> searchApiKeys(@RequestBody final SortablePageInput input) {
        final Page<ApiKey> page = adminApiKeyService.searchApiKeys(input);
        return ResponseEntity.status(OK).body(apiKeyMapper.toPageResource(page));
    }

    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('MANAGE_USERS', 'MANAGE_ORGANIZATIONS')")
    @Operation(summary = "Revoke any API key by ID")
    @DeleteMapping(
        path = ADMIN_API_KEY_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> revokeApiKey(@PathVariable final Long keyId) {
        adminApiKeyService.revokeApiKey(keyId, getLoggedInUser());
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @ResponseStatus(OK)
    @PreAuthorize("hasAnyAuthority('MANAGE_USERS', 'MANAGE_ORGANIZATIONS')")
    @Operation(summary = "Search API key audit log")
    @PostMapping(
        path = ADMIN_API_KEYS_AUDIT_SEARCH_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageResource<ApiKeyAuditResponse>> searchAuditLog(@RequestBody final SortablePageInput input) {
        final Page<ApiKeyAudit> page = adminApiKeyService.searchAuditLog(input);
        return ResponseEntity.status(OK).body(apiKeyAuditMapper.toPageResource(page));
    }
}
