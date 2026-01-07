package io.github.eventify.api.apikey.controller;

import io.github.eventify.api.apikey.model.ApiKey;
import io.github.eventify.api.apikey.model.mapper.ApiKeyMapper;
import io.github.eventify.api.apikey.model.request.CreateApiKeyRequest;
import io.github.eventify.api.apikey.model.response.ApiKeyCreationResponse;
import io.github.eventify.api.apikey.model.response.ApiKeyListResponse;
import io.github.eventify.api.apikey.model.validator.CreateApiKeyValidator;
import io.github.eventify.api.apikey.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.USER_API_KEYS_PATH;
import static io.github.eventify.api.Paths.USER_API_KEY_PATH;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for user API key management.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "User API Keys",
    description = "Manage personal API keys for programmatic access"
)
public class UserApiKeyController {

    private final ApiKeyService apiKeyService;

    private final ApiKeyMapper apiKeyMapper;

    private final CreateApiKeyValidator createApiKeyValidator;

    @PostMapping(
        path = USER_API_KEYS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(CREATED)
    @Operation(
        summary = "Create API key",
        description = "Creates a new personal API key. Full key is only shown once."
    )
    public ResponseEntity<ApiKeyCreationResponse> createApiKey(@RequestBody final CreateApiKeyRequest request) {
        createApiKeyValidator.validateAndThrow(request);
        final ApiKey apiKey = apiKeyService.createUserApiKey(request);
        return ResponseEntity.status(CREATED).body(apiKeyMapper.toCreationResponse(apiKey));
    }

    @GetMapping(
        path = USER_API_KEYS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @Operation(
        summary = "List API keys",
        description = "Lists all personal API keys with masked values."
    )
    public ResponseEntity<ApiKeyListResponse> listApiKeys() {
        final List<ApiKey> keys = apiKeyService.listUserApiKeys();
        final int limit = apiKeyService.getUserKeyLimit();
        return ResponseEntity.status(OK).body(apiKeyMapper.toApiKeyList(keys, limit));
    }

    @DeleteMapping(path = USER_API_KEY_PATH)
    @ResponseStatus(NO_CONTENT)
    @Operation(
        summary = "Revoke API key",
        description = "Revokes and deletes an API key. Creates an audit record."
    )
    public ResponseEntity<Void> revokeApiKey(@PathVariable final Long keyId) {
        apiKeyService.revokeUserApiKey(keyId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
