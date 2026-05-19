package io.github.eventify.api.admin.controller;

import io.github.eventify.api.admin.model.response.AdminStatsResponse;
import io.github.eventify.api.admin.model.response.TableSizeEntry;
import io.github.eventify.api.admin.model.validator.AdminStatsValidator;
import io.github.eventify.api.admin.service.AdminStatsService;
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

import static io.github.eventify.api.Paths.ADMIN_STATS_PATH;
import static io.github.eventify.api.Paths.ADMIN_STATS_STORAGE_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/** Admin dashboard statistics controller. */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Admin Dashboard",
    description = "Endpoints for admin dashboard statistics and platform metrics"
)
public class AdminDashboardController {

    private final AdminStatsService adminStatsService;
    private final AdminStatsValidator adminStatsValidator;

    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('VIEW_PLATFORM_STATS')")
    @Operation(summary = "Get platform statistics for admin dashboard")
    @GetMapping(
        path = ADMIN_STATS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AdminStatsResponse> getStats(
        @RequestParam(defaultValue = "30") final int days) {
        adminStatsValidator.validateAndThrow(days);
        final AdminStatsResponse response = adminStatsService.getAdminStats(days);
        return ResponseEntity.status(OK).body(response);
    }

    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('VIEW_PLATFORM_STATS')")
    @Operation(summary = "Get storage statistics for all tracked database tables")
    @GetMapping(
        path = ADMIN_STATS_STORAGE_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<TableSizeEntry>> getStorageStats() {
        final List<TableSizeEntry> response = adminStatsService.getStorageStats();
        return ResponseEntity.status(OK).body(response);
    }
}
