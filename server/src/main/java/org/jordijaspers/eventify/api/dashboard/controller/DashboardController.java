package org.jordijaspers.eventify.api.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.jordijaspers.eventify.api.dashboard.model.Dashboard;
import org.jordijaspers.eventify.api.dashboard.model.mapper.DashboardMapper;
import org.jordijaspers.eventify.api.dashboard.model.request.CreateDashboardRequest;
import org.jordijaspers.eventify.api.dashboard.model.request.UpdateDashboardDetailsRequest;
import org.jordijaspers.eventify.api.dashboard.model.response.DashboardResponse;
import org.jordijaspers.eventify.api.dashboard.model.validator.DashboardValidator;
import org.jordijaspers.eventify.api.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.jordijaspers.eventify.api.Paths.DASHBOARDS_PATH;
import static org.jordijaspers.eventify.api.Paths.DASHBOARD_PATH;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * The controller for the dashboards.
 */
@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    private final DashboardValidator dashboardValidator;

    private final DashboardMapper dashboardMapper;

    @ResponseStatus(OK)
    @Operation(summary = "Retrieve all dashboards without configuration for the current user.")
    @GetMapping(
        path = DASHBOARDS_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAuthority('READ_DASHBOARDS')")
    public ResponseEntity<List<DashboardResponse>> getDashboards() {
        final List<Dashboard> dashboards = dashboardService.getPermittedDashboards();
        return ResponseEntity.status(OK).body(dashboardMapper.toDashboardResponses(dashboards));
    }

    @ResponseStatus(CREATED)
    @Operation(summary = "Initialize and configure a new dashboard.")
    @PostMapping(
        path = DASHBOARDS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAuthority('WRITE_DASHBOARDS')")
    public ResponseEntity<DashboardResponse> createDashboard(@RequestBody CreateDashboardRequest request) {
        dashboardValidator.validateCreateDashboardRequest(request);
        final Dashboard dashboard = dashboardService.createDashboard(request);
        return ResponseEntity.status(CREATED).body(dashboardMapper.toDashboardResponse(dashboard));
    }

    @ResponseStatus(OK)
    @Operation(summary = "Update the details of a specific dashboard.")
    @PutMapping(
        path = DASHBOARD_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAuthority('WRITE_DASHBOARDS')")
    public ResponseEntity<DashboardResponse> updateDashboard(@PathVariable Long id, @RequestBody UpdateDashboardDetailsRequest request) {
        dashboardValidator.validateUpdateDashboardDetailsRequest(request);
        final Dashboard dashboard = dashboardService.updateDashboard(id, request);
        return ResponseEntity.status(OK).body(dashboardMapper.toDashboardResponse(dashboard));
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Delete a specific dashboard.")
    @DeleteMapping(path = DASHBOARD_PATH)
    @PreAuthorize("hasAuthority('WRITE_DASHBOARDS')")
    public ResponseEntity<Void> deleteDashboard(@PathVariable Long id) {
        dashboardService.deleteDashboard(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
