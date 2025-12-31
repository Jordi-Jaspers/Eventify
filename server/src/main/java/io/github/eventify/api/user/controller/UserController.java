package io.github.eventify.api.user.controller;

import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.mapper.UserDetailsMapper;
import io.github.eventify.api.user.model.request.UpdateUserDetailsRequest;
import io.github.eventify.api.user.model.response.UserDetailsResponse;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.USER_DETAILS;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The controller for the user endpoints.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "User Profile",
    description = "Endpoints for authenticated users to manage their own profile, including viewing and updating user details"
)
public class UserController {

    private final UserService userService;

    private final UserDetailsMapper userDetailsMapper;

    @ResponseStatus(OK)
    @Operation(summary = "Get the details of the authenticated user.")
    @GetMapping(
        path = USER_DETAILS,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDetailsResponse> getUserDetails(@AuthenticationPrincipal final UserTokenPrincipal principal) {
        final UserDetailsResponse response = userDetailsMapper.toResourceObject(principal.getUser());
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
        return ResponseEntity.status(OK).body(userDetailsMapper.toResourceObject(user));
    }
}
