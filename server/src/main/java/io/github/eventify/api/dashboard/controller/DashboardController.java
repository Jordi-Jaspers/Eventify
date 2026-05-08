package io.github.eventify.api.dashboard.controller;

import io.github.eventify.api.dashboard.model.response.DashboardStatsResponse;
import io.github.eventify.api.dashboard.service.DashboardStatsService;
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

import static io.github.eventify.api.Paths.USER_DASHBOARD_STATS_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for personal dashboard operations.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Dashboard",
    description = "Endpoints for personal dashboard statistics"
)
public class DashboardController {

    private final DashboardStatsService dashboardStatsService;

    @ResponseStatus(OK)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get personal dashboard statistics")
    @GetMapping(
        path = USER_DASHBOARD_STATS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DashboardStatsResponse> getPersonalStats(
        @AuthenticationPrincipal final UserTokenPrincipal principal
    ) {
        final DashboardStatsResponse stats = dashboardStatsService.getPersonalStats(
            principal.getUser().getId()
        );
        return ResponseEntity.status(OK).body(stats);
    }
}
