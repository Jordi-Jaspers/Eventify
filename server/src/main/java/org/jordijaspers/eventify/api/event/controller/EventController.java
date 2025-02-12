package org.jordijaspers.eventify.api.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.event.model.validator.EventValidator;
import org.jordijaspers.eventify.api.event.service.EventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.jordijaspers.eventify.api.Paths.EVENTS_PATH;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventPublisher eventPublisher;

    private final EventValidator eventValidator;

    @ResponseStatus(ACCEPTED)
    @Operation(summary = "Submit a new monitoring event")
    @PostMapping(
        path = EVENTS_PATH,
        consumes = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyAuthority('WRITE_EVENTS') and @checkSecurityService.hasCheckPermission(#event.checkId)")
    public ResponseEntity<Void> submitEvent(@RequestBody final EventRequest event) {
        eventValidator.validateAndThrow(event);
        eventPublisher.publish(event);
        return ResponseEntity.status(ACCEPTED).build();
    }
}
