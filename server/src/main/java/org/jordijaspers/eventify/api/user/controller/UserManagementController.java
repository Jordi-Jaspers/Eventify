package org.jordijaspers.eventify.api.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.jordijaspers.eventify.api.authentication.model.response.UserDetailsResponse;
import org.jordijaspers.eventify.api.user.mapper.UserMapper;
import org.jordijaspers.eventify.api.user.model.User;
import org.jordijaspers.eventify.api.user.model.request.UpdateAuthorityRequest;
import org.jordijaspers.eventify.api.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.jordijaspers.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The controller for the user endpoints.
 */
@RestController
@RequiredArgsConstructor
public class UserManagementController {

    private final UserService userService;

    private final UserMapper userMapper;

    @ResponseStatus(OK)
    @Operation(summary = "Lock the specified user, so the user can't login anymore.")
    @PostMapping(
        path = LOCK_USER_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('WRITE_USERS')")
    public ResponseEntity<UserDetailsResponse> lockUser(@PathVariable final Long id) {
        final User user = userService.lockUser(id, true);
        return ResponseEntity.status(OK).body(userMapper.toUserDetailsResponse(user));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Unlock the specified user, so the user can login again.")
    @PostMapping(
        path = UNLOCK_USER_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('WRITE_USERS')")
    public ResponseEntity<UserDetailsResponse> unlockUser(@PathVariable final Long id) {
        final User user = userService.lockUser(id, false);
        return ResponseEntity.status(OK).body(userMapper.toUserDetailsResponse(user));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Update the authority of the specified user.")
    @PostMapping(
        path = USER_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('READ_USERS')")
    public ResponseEntity<UserDetailsResponse> updateAuthority(@PathVariable final Long id,
        @RequestBody final UpdateAuthorityRequest request) {
        final User user = userService.updateAuthority(id, request.getAuthority());
        return ResponseEntity.status(OK).body(userMapper.toUserDetailsResponse(user));
    }
}
