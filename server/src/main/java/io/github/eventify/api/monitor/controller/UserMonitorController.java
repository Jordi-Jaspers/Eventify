package io.github.eventify.api.monitor.controller;

import io.github.eventify.api.monitor.model.MonitorResult;
import io.github.eventify.api.monitor.model.mapper.MonitorMapper;
import io.github.eventify.api.monitor.model.request.MonitorRequest;
import io.github.eventify.api.monitor.model.response.MonitorResponse;
import io.github.eventify.api.monitor.model.validator.MonitorValidator;
import io.github.eventify.api.monitor.service.MonitorService;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.USER_MONITOR_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for user monitor endpoints.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "User Monitor",
    description = "User watchlist timeline monitoring"
)
public class UserMonitorController {

    private final MonitorService monitorService;
    private final MonitorValidator monitorValidator;
    private final MonitorMapper monitorMapper;

    @ResponseStatus(OK)
    @Operation(
        summary = "Get user monitor timeline",
        description = "Returns timeline data for a user watchlist"
    )
    @PostMapping(
        path = USER_MONITOR_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@watchlistSecurity.canAccessUserWatchlist(#request.watchlistId, principal.user.id)")
    public ResponseEntity<MonitorResponse> getUserMonitor(@AuthenticationPrincipal final UserTokenPrincipal principal,
        @RequestBody final MonitorRequest request) {
        monitorValidator.validateAndThrow(request);
        final MonitorResult result = monitorService.monitorWatchlist(request);
        return ResponseEntity.status(OK).body(monitorMapper.toResponse(result));
    }
}
