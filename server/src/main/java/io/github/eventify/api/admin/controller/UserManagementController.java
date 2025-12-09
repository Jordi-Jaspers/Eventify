package io.github.eventify.api.admin.controller;

import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.model.mapper.UserMapper;
import io.github.eventify.api.user.model.request.UpdateRoleRequest;
import io.github.eventify.api.user.model.response.UserDetailsResponse;
import io.github.eventify.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The controller for the user endpoints.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "User Management (Admin)",
    description = "Administrative endpoints for managing users, roles, and account status"
)
public class UserManagementController {

    private final UserService userService;

    private final UserMapper userMapper;

    @ResponseStatus(OK)
    @Operation(summary = "Get a list of all the users.")
    @GetMapping(
        path = USERS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('MANAGE_USERS')")
    public ResponseEntity<List<UserDetailsResponse>> getListUsers() {
        final List<User> users = userService.getAllUsers();
        return ResponseEntity.status(OK).body(userMapper.toUserDetailsResponseList(users));
    }


    @ResponseStatus(OK)
    @Operation(summary = "Lock the specified user, so the user can't login anymore.")
    @PostMapping(
        path = LOCK_USER_PATH,
        produces = APPLICATION_JSON_VALUE,
        consumes = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('MANAGE_USERS')")
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
    @PreAuthorize("hasAnyAuthority('MANAGE_USERS')")
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
    @PreAuthorize("hasAnyAuthority('MANAGE_USERS')")
    public ResponseEntity<UserDetailsResponse> updateRole(@PathVariable final Long id,
        @RequestBody final UpdateRoleRequest request) {
        final User user = userService.updateAuthority(id, request.getRole());
        return ResponseEntity.status(OK).body(userMapper.toUserDetailsResponse(user));
    }
}
