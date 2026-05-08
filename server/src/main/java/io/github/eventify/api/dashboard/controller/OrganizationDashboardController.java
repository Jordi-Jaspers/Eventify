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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.ORGANIZATION_DASHBOARD_STATS_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for organization dashboard operations.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Organization Dashboard",
    description = "Endpoints for organization dashboard statistics"
)
public class OrganizationDashboardController {

    private final DashboardStatsService dashboardStatsService;

    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isMember(#orgId, #principal.user.id)")
    @Operation(summary = "Get organization dashboard statistics")
    @GetMapping(
        path = ORGANIZATION_DASHBOARD_STATS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<DashboardStatsResponse> getOrganizationStats(
        @PathVariable final Long orgId,
        @AuthenticationPrincipal final UserTokenPrincipal principal
    ) {
        final DashboardStatsResponse stats = dashboardStatsService.getOrganizationStats(orgId);
        return ResponseEntity.status(OK).body(stats);
    }
}
