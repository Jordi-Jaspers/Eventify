package io.github.eventify.api.admin.controller;

import io.github.eventify.api.admin.model.AdminCounts;
import io.github.eventify.api.admin.model.AdminEventVolume;
import io.github.eventify.api.admin.model.AdminGrowth;
import io.github.eventify.api.admin.model.EventStats;
import io.github.eventify.api.admin.model.StorageStats;
import io.github.eventify.api.admin.model.mapper.AdminStatsMapper;
import io.github.eventify.api.admin.model.response.AdminCountsResponse;
import io.github.eventify.api.admin.model.response.AdminEventStatsResponse;
import io.github.eventify.api.admin.model.response.AdminEventVolumeResponse;
import io.github.eventify.api.admin.model.response.AdminGrowthResponse;
import io.github.eventify.api.admin.model.response.TableSizeEntry;
import io.github.eventify.api.admin.model.validator.AdminStatsValidator;
import io.github.eventify.api.admin.service.AdminEventStatsService;
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

import static io.github.eventify.api.Paths.ADMIN_STATS_COUNTS_PATH;
import static io.github.eventify.api.Paths.ADMIN_STATS_EVENTS_PATH;
import static io.github.eventify.api.Paths.ADMIN_STATS_EVENT_VOLUME_PATH;
import static io.github.eventify.api.Paths.ADMIN_STATS_GROWTH_PATH;
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
    private final AdminEventStatsService adminEventStatsService;
    private final AdminStatsValidator adminStatsValidator;
    private final AdminStatsMapper adminStatsMapper;

    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('VIEW_PLATFORM_STATS')")
    @Operation(summary = "Get platform counts for admin dashboard")
    @GetMapping(
        path = ADMIN_STATS_COUNTS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AdminCountsResponse> getCounts() {
        final AdminCounts counts = adminStatsService.getAdminCounts();
        return ResponseEntity.status(OK).body(adminStatsMapper.toCountsResponse(counts));
    }

    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('VIEW_PLATFORM_STATS')")
    @Operation(summary = "Get growth data for admin dashboard")
    @GetMapping(
        path = ADMIN_STATS_GROWTH_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AdminGrowthResponse> getGrowth(
        @RequestParam(defaultValue = "30") final int days) {
        adminStatsValidator.validateAndThrow(days);
        final AdminGrowth growth = adminStatsService.getAdminGrowth(days);
        return ResponseEntity.status(OK).body(adminStatsMapper.toGrowthResponse(growth));
    }

    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('VIEW_PLATFORM_STATS')")
    @Operation(summary = "Get event volume for admin dashboard")
    @GetMapping(
        path = ADMIN_STATS_EVENT_VOLUME_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AdminEventVolumeResponse> getEventVolume(
        @RequestParam(defaultValue = "30") final int days) {
        adminStatsValidator.validateAndThrow(days);
        final AdminEventVolume volume = adminStatsService.getEventVolume(days);
        return ResponseEntity.status(OK).body(adminStatsMapper.toEventVolumeResponse(volume));
    }

    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('VIEW_PLATFORM_STATS')")
    @Operation(summary = "Get event statistics for admin dashboard")
    @GetMapping(
        path = ADMIN_STATS_EVENTS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AdminEventStatsResponse> getEventStats(
        @RequestParam(defaultValue = "30") final int days) {
        adminStatsValidator.validateAndThrow(days);
        final EventStats data = adminEventStatsService.getEventStats(days);
        return ResponseEntity.status(OK).body(adminStatsMapper.toEventStatsResponse(data));
    }

    @ResponseStatus(OK)
    @PreAuthorize("hasAuthority('VIEW_PLATFORM_STATS')")
    @Operation(summary = "Get storage statistics for all tracked database tables")
    @GetMapping(
        path = ADMIN_STATS_STORAGE_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<TableSizeEntry>> getStorageStats() {
        final List<StorageStats> data = adminStatsService.getStorageStats();
        return ResponseEntity.status(OK).body(adminStatsMapper.toTableSizeEntryList(data));
    }
}
