package io.github.eventify.api.admin.controller;

import io.github.eventify.api.admin.model.validator.UserSearchValidator;
import io.github.eventify.api.admin.service.AdminUserService;
import io.github.eventify.api.user.model.response.UserSearchResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.ADMIN_USERS_SEARCH_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for admin user management operations.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Admin User Management",
    description = "Endpoints for admin user search and management"
)
public class AdminUserController {

    private final AdminUserService adminUserService;

    private final UserSearchValidator userSearchValidator;

    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Search for users by email or name")
    @GetMapping(
        path = ADMIN_USERS_SEARCH_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<UserSearchResult>> searchUsers(
        @RequestParam final String query
    ) {
        userSearchValidator.validateAndThrow(query);
        final List<UserSearchResult> results = adminUserService.searchUsers(query);
        return ResponseEntity.status(OK).body(results);
    }
}
