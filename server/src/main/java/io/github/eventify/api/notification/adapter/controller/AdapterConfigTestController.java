package io.github.eventify.api.notification.adapter.controller;

import io.github.eventify.api.notification.adapter.model.request.TestAdapterConnectionRequest;
import io.github.eventify.api.notification.adapter.model.response.TestConnectionResponse;
import io.github.eventify.api.notification.adapter.model.validator.TestAdapterConnectionValidator;
import io.github.eventify.api.notification.adapter.service.AdapterTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.ADAPTER_CONFIG_TEST_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for testing adapter connectivity without persisting a configuration.
 */
@Tag(
    name = "Adapter Configs",
    description = "Adapter configuration operations"
)
@RestController
@RequiredArgsConstructor
public class AdapterConfigTestController {

    private final AdapterTestService adapterTestService;
    private final TestAdapterConnectionValidator testAdapterConnectionValidator;

    @PostMapping(
        path = ADAPTER_CONFIG_TEST_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Test adapter connection",
        description = "Sends a test notification to the given webhook URL using the specified adapter type"
    )
    @SuppressWarnings("PMD.UnitTestShouldUseTestAnnotation")
    public ResponseEntity<TestConnectionResponse> testConnection(
        @RequestBody final TestAdapterConnectionRequest request) {
        testAdapterConnectionValidator.validateAndThrow(request);
        return ResponseEntity.status(OK).body(
            adapterTestService.testConnection(request.getAdapterType(), request.getWebhookUrl())
        );
    }
}
