package io.github.eventify.api.notification.adapter.controller;

import io.github.eventify.api.notification.adapter.model.mapper.AdapterConfigMapper;
import io.github.eventify.api.notification.adapter.model.request.CreateAdapterConfigRequest;
import io.github.eventify.api.notification.adapter.model.request.UpdateAdapterConfigRequest;
import io.github.eventify.api.notification.adapter.model.response.AdapterConfigResponse;
import io.github.eventify.api.notification.adapter.model.validator.AdapterConfigValidator;
import io.github.eventify.api.notification.adapter.service.UserAdapterConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.USER_ADAPTER_CONFIGS_PATH;
import static io.github.eventify.api.Paths.USER_ADAPTER_CONFIG_PATH;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for managing personal adapter configurations.
 */
@Tag(
    name = "Adapter Configs",
    description = "Adapter configuration operations"
)
@RestController
@RequiredArgsConstructor
public class UserAdapterConfigController {

    private final UserAdapterConfigService userAdapterConfigService;
    private final AdapterConfigValidator adapterConfigValidator;
    private final AdapterConfigMapper adapterConfigMapper;

    @PostMapping(
        path = USER_ADAPTER_CONFIGS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create personal adapter config",
        description = "Creates a personal adapter configuration for the current user"
    )
    public ResponseEntity<AdapterConfigResponse> createPersonal(
        @RequestBody final CreateAdapterConfigRequest request) {
        adapterConfigValidator.validateAndThrow(request);
        return ResponseEntity.status(CREATED).body(
            adapterConfigMapper.toResourceObject(userAdapterConfigService.createPersonal(request))
        );
    }

    @GetMapping(
        path = USER_ADAPTER_CONFIGS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "List personal adapter configs",
        description = "Lists personal adapter configurations for the current user"
    )
    public ResponseEntity<List<AdapterConfigResponse>> listPersonal() {
        return ResponseEntity.ok(
            adapterConfigMapper.toResourceObjects(userAdapterConfigService.listPersonal())
        );
    }

    @GetMapping(
        path = USER_ADAPTER_CONFIG_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@adapterConfigSecurity.canAccessPersonalConfig(#id, principal.user.id)")
    @Operation(
        summary = "Get adapter config",
        description = "Returns an adapter configuration by ID"
    )
    public ResponseEntity<AdapterConfigResponse> get(@PathVariable final Long id) {
        return ResponseEntity.ok(adapterConfigMapper.toResourceObject(userAdapterConfigService.get(id)));
    }

    @PutMapping(
        path = USER_ADAPTER_CONFIG_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@adapterConfigSecurity.canAccessPersonalConfig(#id, principal.user.id)")
    @Operation(
        summary = "Update adapter config",
        description = "Updates an adapter configuration by ID"
    )
    public ResponseEntity<AdapterConfigResponse> update(
        @PathVariable final Long id,
        @RequestBody final UpdateAdapterConfigRequest request) {
        return ResponseEntity.ok(
            adapterConfigMapper.toResourceObject(userAdapterConfigService.update(id, request))
        );
    }

    @DeleteMapping(path = USER_ADAPTER_CONFIG_PATH)
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("@adapterConfigSecurity.canAccessPersonalConfig(#id, principal.user.id)")
    @Operation(
        summary = "Delete adapter config",
        description = "Deletes an adapter configuration by ID"
    )
    public ResponseEntity<Void> delete(@PathVariable final Long id) {
        userAdapterConfigService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
