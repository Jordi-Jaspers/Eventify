package org.jordijaspers.eventify.api.monitoring.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Optional;

import org.jordijaspers.eventify.api.monitoring.service.TimelineStreamingService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.jordijaspers.eventify.api.Paths.MONITORING_STREAM_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequiredArgsConstructor
public class MonitoringController {

    private final TimelineStreamingService streamingService;

    @ResponseStatus(OK)
    @Operation(summary = "Subscribe to real-time monitoring data for a specific dashboard.")
    @GetMapping(
        path = MONITORING_STREAM_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public SseEmitter streamDashboard(@PathVariable final Long id, @RequestParam(required = false) final Duration window) {
        return streamingService.subscribe(id, Optional.ofNullable(window));
    }
}
