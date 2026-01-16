package io.github.eventify.api.user.controller;

import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.request.UpdateRetentionRequest;
import io.github.eventify.api.user.model.response.RetentionSettingsResponse;
import io.github.eventify.api.user.model.validator.RetentionValidator;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.github.jframe.validation.ValidationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.USER_RETENTION_SETTINGS_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for user retention settings endpoints.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "User Retention Settings",
    description = "Endpoints for managing user data retention settings"
)
public class UserSettingsController {

    private final UserService userService;
    private final RetentionValidator retentionValidator;

    @ResponseStatus(OK)
    @Operation(summary = "Get retention settings for authenticated user")
    @GetMapping(
        path = USER_RETENTION_SETTINGS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RetentionSettingsResponse> getRetentionSettings(@AuthenticationPrincipal final UserTokenPrincipal principal) {
        final User user = principal.getUser();
        final RetentionSettingsResponse response = new RetentionSettingsResponse();
        response.setRetentionDays(user.getRetentionDays());
        return ResponseEntity.status(OK).body(response);
    }

    @ResponseStatus(OK)
    @Operation(summary = "Update retention settings for authenticated user")
    @PutMapping(
        path = USER_RETENTION_SETTINGS_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RetentionSettingsResponse> updateRetentionSettings(
        @RequestBody final UpdateRetentionRequest request,
        @AuthenticationPrincipal final UserTokenPrincipal principal
    ) {
        retentionValidator.validate(request, new ValidationResult());
        final User user = userService.updateRetentionDays(principal.getUser(), request.getRetentionDays());
        final RetentionSettingsResponse response = new RetentionSettingsResponse();
        response.setRetentionDays(user.getRetentionDays());
        return ResponseEntity.status(OK).body(response);
    }
}
