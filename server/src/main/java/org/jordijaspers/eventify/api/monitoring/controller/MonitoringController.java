package org.jordijaspers.eventify.api.monitoring.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

import org.jordijaspers.eventify.api.monitoring.model.validator.WindowValidator;
import org.jordijaspers.eventify.api.monitoring.service.TimelineStreamingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.jordijaspers.eventify.api.Paths.MONITORING_STREAM_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@RestController
@RequiredArgsConstructor
public class MonitoringController {

    private final TimelineStreamingService streamingService;

    private final WindowValidator windowValidator;

    @ResponseStatus(OK)
    @Operation(summary = "Subscribe to real-time monitoring data for a specific dashboard.")
    @GetMapping(
        path = MONITORING_STREAM_PATH,
        produces = {
            TEXT_EVENT_STREAM_VALUE,
            APPLICATION_JSON_VALUE
        }
    )
    @PreAuthorize("hasAuthority('READ_DASHBOARDS') and @dashboardSecurityService.hasDashboardAccess(#id)")
    public SseEmitter streamDashboard(@RequestParam(
        required = false,
        defaultValue = "PT2H"
    ) final Duration window,
        @PathVariable final Long id) {
        windowValidator.validateAndThrow(window);
        return streamingService.subscribe(id, window);
    }
}
