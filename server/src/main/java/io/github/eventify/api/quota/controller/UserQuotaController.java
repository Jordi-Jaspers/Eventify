package io.github.eventify.api.quota.controller;

import io.github.eventify.api.quota.model.response.UserQuotaResponse;
import io.github.eventify.api.quota.service.UserQuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.USER_QUOTA_PATH;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for user event quota endpoints.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "User Quota",
    description = "User event quota management"
)
public class UserQuotaController {

    private final UserQuotaService userQuotaService;

    @GetMapping(
        path = USER_QUOTA_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @Operation(summary = "Get current event quota status for the authenticated user")
    public ResponseEntity<UserQuotaResponse> getQuota() {
        final UserQuotaResponse response = userQuotaService.getQuotaStatus(getLoggedInUser().getId());
        return ResponseEntity.status(OK).body(response);
    }
}
