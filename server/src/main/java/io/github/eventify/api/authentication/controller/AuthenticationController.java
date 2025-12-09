package io.github.eventify.api.authentication.controller;

import io.github.eventify.api.authentication.model.request.LoginRequest;
import io.github.eventify.api.authentication.model.request.RefreshTokenRequest;
import io.github.eventify.api.authentication.model.request.RegisterUserRequest;
import io.github.eventify.api.authentication.model.response.AuthenticationResponse;
import io.github.eventify.api.authentication.model.response.RegisterResponse;
import io.github.eventify.api.authentication.model.validator.AuthenticationValidator;
import io.github.eventify.api.authentication.service.AuthenticationService;
import io.github.eventify.api.authentication.service.CookieService;
import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The controller to handle authentication-related operations such as registration, login, token refresh, logout, email verification, and
 * resending verification emails.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Authentication",
    description = "Traditional email/password authentication endpoints for user registration, login, token refresh, and logout"
)
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
    public ResponseEntity<AuthenticationResponse> authorize(@RequestBody final LoginRequest request, final HttpServletResponse response) {
        validator.validateLoginRequest(request);
        final User user = authenticationService.authorize(request.getEmail(), request.getPassword());
        cookieService.setAuthCookies(response, user.getAccessToken(), user.getRefreshToken());
        return ResponseEntity.status(OK).body(userMapper.toUserResponse(user));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Refresh the access token by exchanging the refresh token.")
    @PostMapping(path = TOKEN_PATH)
    public ResponseEntity<AuthenticationResponse> refreshTokens(@RequestBody final RefreshTokenRequest request,
        final HttpServletResponse response) {
        final User user = authenticationService.refresh(request.getRefreshToken());
        cookieService.setAuthCookies(response, user.getAccessToken(), user.getRefreshToken());
        return ResponseEntity.status(OK).body(userMapper.toUserResponse(user));
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Revoke the refresh token and log out the user.")
    @GetMapping(path = LOGOUT_PATH)
    public ResponseEntity<Void> logout(final HttpServletResponse response) {
        authenticationService.logout();
        cookieService.clearAuthCookies(response);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
