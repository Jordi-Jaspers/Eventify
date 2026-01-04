package io.github.eventify.api.admin.controller;

import io.github.eventify.api.admin.model.response.AdminStatsResponse;
import io.github.eventify.api.admin.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.ADMIN_STATS_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for admin dashboard operations.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Admin Dashboard",
    description = "Endpoints for admin dashboard statistics and platform metrics"
)
public class AdminDashboardController {

    private final AdminStatsService adminStatsService;

    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('VIEW_PLATFORM_STATS')")
    @Operation(summary = "Get platform statistics for admin dashboard")
    @GetMapping(
        path = ADMIN_STATS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AdminStatsResponse> getStats() {
        final AdminStatsResponse response = adminStatsService.getAdminStats();
        return ResponseEntity.status(OK).body(response);
    }
}
