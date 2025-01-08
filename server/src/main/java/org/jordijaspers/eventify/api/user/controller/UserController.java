package org.jordijaspers.eventify.api.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.jordijaspers.eventify.api.authentication.model.response.UserDetailsResponse;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.model.mapper.UserMapper;
import org.jordijaspers.eventify.api.user.model.request.UpdateEmailRequest;
import org.jordijaspers.eventify.api.user.model.request.UpdateUserDetailsRequest;
import org.jordijaspers.eventify.api.user.model.validator.EmailValidator;
import org.jordijaspers.eventify.api.user.service.UserService;
import org.jordijaspers.eventify.common.security.principal.UserTokenPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.jordijaspers.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The controller for the user endpoints.
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final UserMapper userMapper;

    private final EmailValidator emailValidator;

    @ResponseStatus(OK)
    @Operation(summary = "Get a list of all the users.")
    @GetMapping(
        path = USERS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('READ_USERS')")
    public ResponseEntity<List<UserDetailsResponse>> getListUsers() {
        final List<User> users = userService.getAllUsers();
        return ResponseEntity.status(OK).body(userMapper.toUserDetailsResponseList(users));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Get the details of the authenticated user.")
    @GetMapping(
        path = USER_DETAILS,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDetailsResponse> getUserDetails(@AuthenticationPrincipal final UserTokenPrincipal principal) {
        final UserDetailsResponse response = userMapper.toUserDetailsResponse(principal.getUser());
        return ResponseEntity.status(OK).body(response);
    }

    @ResponseStatus(OK)
    @Operation(summary = "Update the details of the authenticated user.")
    @PostMapping(
        path = USER_DETAILS,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDetailsResponse> updateUserDetails(@RequestBody final UpdateUserDetailsRequest request,
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        final User user = userService.updateUserDetails(principal.getUser(), request);
        return ResponseEntity.status(OK).body(userMapper.toUserDetailsResponse(user));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Update the email of the authenticated user.")
    @PostMapping(
        path = USER_UPDATE_EMAIL_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDetailsResponse> updateUserEmail(@RequestBody final UpdateEmailRequest request,
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        emailValidator.validateAndThrow(request);
        final User user = userService.updateEmail(principal.getUser(), request.getEmail());
        return ResponseEntity.status(OK).body(userMapper.toUserDetailsResponse(user));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Validate the email address and check if it is already in use.")
    @PostMapping(
        path = PUBLIC_VALIDATE_EMAIL_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Boolean> validateEmail(@RequestBody final UpdateEmailRequest request) {
        final boolean valid = emailValidator.validate(request).hasErrors() && userService.isEmailInAlreadyUse(request.getEmail());
        return ResponseEntity.status(OK).body(valid);
    }
}
