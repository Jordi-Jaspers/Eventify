package io.github.eventify.api.dashboard.controller;

import io.github.eventify.api.dashboard.model.UserDashboard;
import io.github.eventify.api.dashboard.model.mapper.UserDashboardMapper;
import io.github.eventify.api.dashboard.model.response.UserDashboardResponse;
import io.github.eventify.api.dashboard.service.UserDashboardService;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.USER_DASHBOARD_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** Controller for user dashboard aggregation. */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Dashboard",
    description = "Endpoints for user dashboard aggregation"
)
public class UserDashboardController {

    private final UserDashboardService userDashboardService;
    private final UserDashboardMapper userDashboardMapper;

    @ResponseStatus(OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get user dashboard",
        description = "Returns aggregated dashboard data for the authenticated user"
    )
    @GetMapping(
        path = USER_DASHBOARD_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDashboardResponse> getDashboard(
        @AuthenticationPrincipal final UserTokenPrincipal principal
    ) {
        final UserDashboard dashboard = userDashboardService.getDashboard(principal.getUser().getId());
        return ResponseEntity.status(OK).body(userDashboardMapper.toResponse(dashboard));
    }
}
