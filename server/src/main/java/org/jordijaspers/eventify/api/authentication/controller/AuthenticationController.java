package org.jordijaspers.eventify.api.authentication.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletResponse;

import org.jordijaspers.eventify.api.authentication.model.request.LoginRequest;
import org.jordijaspers.eventify.api.authentication.model.request.RefreshTokenRequest;
import org.jordijaspers.eventify.api.authentication.model.request.RegisterUserRequest;
import org.jordijaspers.eventify.api.authentication.model.response.RegisterResponse;
import org.jordijaspers.eventify.api.authentication.model.response.UserResponse;
import org.jordijaspers.eventify.api.authentication.model.validator.AuthenticationValidator;
import org.jordijaspers.eventify.api.authentication.service.AuthenticationService;
import org.jordijaspers.eventify.api.authentication.service.CookieService;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.model.mapper.UserMapper;
import org.jordijaspers.eventify.common.security.principal.UserTokenPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static java.util.Objects.nonNull;
import static org.jordijaspers.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    private final AuthenticationValidator validator;

    private final CookieService cookieService;

    private final UserMapper userMapper;

    @ResponseStatus(CREATED)
    @Operation(summary = "Register a new user.")
    @PostMapping(
        path = REGISTER_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<RegisterResponse> register(@RequestBody final RegisterUserRequest request) {
        validator.validateUserRegistration(request);
        final User user = authenticationService.register(userMapper.toUser(request), request.getPassword());
        return ResponseEntity.status(CREATED).body(userMapper.toRegisterResponse(user));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Login with an existing user using email and password.")
    @PostMapping(
        path = LOGIN_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserResponse> authorize(@RequestBody final LoginRequest request, final HttpServletResponse response) {
        validator.validateLoginRequest(request);
        final User user = authenticationService.authorize(request.getEmail(), request.getPassword());
        cookieService.setAuthCookies(response, user.getAccessToken(), user.getRefreshToken());
        return ResponseEntity.status(OK).body(userMapper.toUserResponse(user));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Refresh the access token by exchanging the refresh token.")
    @PostMapping(path = TOKEN_PATH)
    public ResponseEntity<UserResponse> refreshTokens(@RequestBody final RefreshTokenRequest request, final HttpServletResponse response) {
        final User user = authenticationService.refresh(request.getRefreshToken());
        cookieService.setAuthCookies(response, user.getAccessToken(), user.getRefreshToken());
        return ResponseEntity.status(OK).body(userMapper.toUserResponse(user));
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Revoke the refresh token and log out the user.")
    @GetMapping(path = LOGOUT_PATH)
    public ResponseEntity<Void> logout(@AuthenticationPrincipal final UserTokenPrincipal principal, final HttpServletResponse response) {
        if (nonNull(principal)) {
            authenticationService.logout(principal.getUser());
            cookieService.clearAuthCookies(response);
        }
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @ResponseStatus(OK)
    @Operation(summary = "Verify the email address of a user.")
    @PostMapping(path = VERIFICATION_PATH)
    public ResponseEntity<UserResponse> verifyEmail(@RequestParam("token") final String token) {
        final User user = authenticationService.verifyEmail(token);
        return ResponseEntity.status(OK).body(userMapper.toUserResponse(user));
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Resend the verification email to the user.")
    @PostMapping(path = RESEND_EMAIL_VERIFICATION_PATH)
    public ResponseEntity<Void> resendEmailVerification(@RequestParam("email") final String email) {
        authenticationService.resendVerification(email);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
