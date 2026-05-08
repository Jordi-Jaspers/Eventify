package io.github.eventify.api.authentication.controller;

import io.github.eventify.api.authentication.model.response.AuthenticationResponse;
import io.github.eventify.api.authentication.service.AuthenticationService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.mapper.UserDetailsMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.RESEND_EMAIL_VERIFICATION_PATH;
import static io.github.eventify.api.Paths.VERIFICATION_PATH;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

/**
 * The controller to handle authentication-related operations such as registration, login, token refresh, logout, email verification, and
 * resending verification emails.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Email Verification",
    description = "Endpoints for email verification and resending verification emails"
)
public class EmailVerificationController {

    private final AuthenticationService authenticationService;

    private final UserDetailsMapper userDetailsMapper;

    @ResponseStatus(OK)
    @Operation(summary = "Verify the email address of a user.")
    @PostMapping(path = VERIFICATION_PATH)
    public ResponseEntity<AuthenticationResponse> verifyEmail(@RequestParam("token") final String token, final HttpServletRequest request) {
        final User user = authenticationService.verifyEmail(token, request);
        return ResponseEntity.status(OK).body(userDetailsMapper.toUserResponse(user));
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Resend the verification email to the user.")
    @PostMapping(path = RESEND_EMAIL_VERIFICATION_PATH)
    public ResponseEntity<Void> resendEmailVerification(@RequestParam("email") final String email) {
        authenticationService.resendVerification(email);
        return ResponseEntity.status(NO_CONTENT).build();
    }

}
