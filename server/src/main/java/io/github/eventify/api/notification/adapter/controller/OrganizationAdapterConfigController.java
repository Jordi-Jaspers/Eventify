package io.github.eventify.api.notification.adapter.controller;

import io.github.eventify.api.notification.adapter.model.mapper.AdapterConfigMapper;
import io.github.eventify.api.notification.adapter.model.request.CreateAdapterConfigRequest;
import io.github.eventify.api.notification.adapter.model.response.AdapterConfigResponse;
import io.github.eventify.api.notification.adapter.model.validator.AdapterConfigValidator;
import io.github.eventify.api.notification.adapter.service.OrganizationAdapterConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.ORGANIZATION_ADAPTER_CONFIGS_PATH;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for managing organization-scoped adapter configurations.
 */
@Tag(
    name = "Adapter Configs",
    description = "Adapter configuration operations"
)
@RestController
@RequiredArgsConstructor
public class OrganizationAdapterConfigController {

    private final OrganizationAdapterConfigService organizationAdapterConfigService;
    private final AdapterConfigValidator adapterConfigValidator;
    private final AdapterConfigMapper adapterConfigMapper;

    @PostMapping(
        path = ORGANIZATION_ADAPTER_CONFIGS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(CREATED)
    @PreAuthorize("@adapterConfigSecurity.canManageOrgConfigs(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Create org adapter config",
        description = "Creates an adapter configuration for an organization"
    )
    public ResponseEntity<AdapterConfigResponse> createForOrganization(
        @PathVariable final Long orgId,
        @RequestBody final CreateAdapterConfigRequest request) {
        adapterConfigValidator.validateAndThrow(request);
        return ResponseEntity.status(CREATED).body(
            adapterConfigMapper.toResourceObject(
                organizationAdapterConfigService.createForOrganization(orgId, request)
            )
        );
    }

    @GetMapping(
        path = ORGANIZATION_ADAPTER_CONFIGS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@adapterConfigSecurity.canManageOrgConfigs(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "List org adapter configs",
        description = "Lists adapter configurations for an organization"
    )
    public ResponseEntity<List<AdapterConfigResponse>> listForOrganization(
        @PathVariable final Long orgId) {
        return ResponseEntity.ok(
            adapterConfigMapper.toResourceObjects(
                organizationAdapterConfigService.listForOrganization(orgId)
            )
        );
    }
}
