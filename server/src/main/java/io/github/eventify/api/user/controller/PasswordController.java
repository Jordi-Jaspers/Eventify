package io.github.eventify.api.user.controller;

import io.github.eventify.api.user.model.request.ForgotPasswordRequest;
import io.github.eventify.api.user.model.request.UpdatePasswordRequest;
import io.github.eventify.api.user.model.validator.ChangePasswordValidator;
import io.github.eventify.api.user.service.PasswordService;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The controller for the password endpoints.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Password Management",
    description = "Endpoints for password-related operations including password reset requests, password resets, and password updates"
)
public class PasswordController {

    private final PasswordService passwordService;

    private final ChangePasswordValidator passwordValidator;

    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Request a password reset.")
    @PostMapping(path = PUBLIC_REQUEST_PASSWORD_RESET_PATH)
    public ResponseEntity<Void> requestPasswordReset(@RequestParam(name = "email") final String email) {
        passwordService.requestPasswordReset(email);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Reset the password with the provided token.")
    @PostMapping(
        path = PUBLIC_RESET_PASSWORD_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> resetPassword(@RequestBody final ForgotPasswordRequest request) {
        passwordValidator.validateAndThrow(request);
        passwordService.changePassword(request.getNewPassword(), request.getToken());
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Update the password of the authenticated user.")
    @PostMapping(
        path = UPDATE_PASSWORD_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> updatePassword(@RequestBody final UpdatePasswordRequest request,
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        passwordValidator.validateAndThrow(request);
        passwordService.updatePassword(request, principal.getUser());
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
