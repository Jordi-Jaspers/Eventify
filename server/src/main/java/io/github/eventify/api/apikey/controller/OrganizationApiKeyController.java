package io.github.eventify.api.apikey.controller;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.mapper.ApiKeyMapper;
import io.github.eventify.api.apikey.model.request.CreateApiKeyRequest;
import io.github.eventify.api.apikey.model.response.ApiKeyCreationResponse;
import io.github.eventify.api.apikey.model.response.ApiKeyResponse;
import io.github.eventify.api.apikey.model.validator.CreateApiKeyValidator;
import io.github.eventify.api.apikey.service.ApiKeyService;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.ORGANIZATION_API_KEYS_PATH;
import static io.github.eventify.api.Paths.ORGANIZATION_API_KEYS_SEARCH_PATH;
import static io.github.eventify.api.Paths.ORGANIZATION_API_KEY_PATH;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for organization API key management.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Organization API Keys",
    description = "Manage API keys for organization-level programmatic access"
)
public class OrganizationApiKeyController {

    private final ApiKeyService apiKeyService;

    private final ApiKeyMapper apiKeyMapper;

    private final CreateApiKeyValidator createApiKeyValidator;

    @PostMapping(
        path = ORGANIZATION_API_KEYS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(CREATED)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Create organization API key",
        description = "Creates a new organization API key. Full key is only shown once. Requires OWNER or ADMIN role."
    )
    public ResponseEntity<ApiKeyCreationResponse> createOrganizationApiKey(@PathVariable final Long orgId,
        @RequestBody final CreateApiKeyRequest request) {
        createApiKeyValidator.validateAndThrow(request);
        final ApiKey apiKey = apiKeyService.createOrganizationApiKey(orgId, getLoggedInUser(), request);
        return ResponseEntity.status(CREATED).body(apiKeyMapper.toCreationResponse(apiKey));
    }

    @PostMapping(
        path = ORGANIZATION_API_KEYS_SEARCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isMember(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Search organization API keys",
        description = "Search organization API keys with pagination, filtering, and sorting. Any organization member can view."
    )
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<PageResource<ApiKeyResponse>> searchOrganizationApiKeys(@PathVariable final Long orgId,
        @RequestBody final SortablePageInput input) {
        final Page<ApiKey> page = apiKeyService.searchOrganizationApiKeys(input, orgId);
        return ResponseEntity.status(OK).body(apiKeyMapper.toPageResource(page));
    }

    @DeleteMapping(path = ORGANIZATION_API_KEY_PATH)
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Revoke organization API key",
        description = "Revokes and deletes an organization API key. Creates an audit record. Requires OWNER or ADMIN role."
    )
    public ResponseEntity<Void> revokeOrganizationApiKey(@PathVariable final Long orgId,
        @PathVariable final Long keyId) {
        apiKeyService.revokeApiKey(keyId, getLoggedInUser(), orgId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
