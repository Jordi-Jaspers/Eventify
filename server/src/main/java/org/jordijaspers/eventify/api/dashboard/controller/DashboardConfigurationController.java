package org.jordijaspers.eventify.api.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.model.mapper.DashboardMapper;
import org.jordijaspers.eventify.api.dashboard.model.request.DashboardConfigurationRequest;
import org.jordijaspers.eventify.api.dashboard.model.response.DashboardResponse;
import org.jordijaspers.eventify.api.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.jordijaspers.eventify.api.Paths.DASHBOARD_CONFIGURATION_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The controller for the dashboard configuration.
 */
@RestController
@RequiredArgsConstructor
public class DashboardConfigurationController {

    private final DashboardService dashboardService;

    private final DashboardMapper dashboardMapper;

    @ResponseStatus(OK)
    @Operation(summary = "Retrieve a specific dashboard with its configuration.")
    @GetMapping(
        path = DASHBOARD_CONFIGURATION_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAuthority('READ_DASHBOARDS')")
    public ResponseEntity<DashboardResponse> getDashboardConfiguration(@PathVariable Long id) {
        final Dashboard dashboard = dashboardService.getDashboardConfiguration(id);
        return ResponseEntity.status(OK).body(dashboardMapper.toDashboardResponse(dashboard));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Configure an existing dashboard.")
    @PutMapping(DASHBOARD_CONFIGURATION_PATH)
    @PreAuthorize("hasAuthority('WRITE_DASHBOARDS')")
    public ResponseEntity<DashboardResponse> configureDashboard(
        @PathVariable Long id,
        @RequestBody DashboardConfigurationRequest request) {
        final Dashboard dashboard = dashboardService.configureDashboard(id, request);
        return ResponseEntity.status(OK).body(dashboardMapper.toDashboardResponse(dashboard));
    }
}
