package org.jordijaspers.eventify.api.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jordijaspers.eventify.api.event.model.request.EventRequest;
import org.jordijaspers.eventify.api.event.model.validator.EventValidator;
import org.jordijaspers.eventify.api.event.service.EventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EventController {

    private final EventPublisher eventPublisher;

    private final EventValidator eventValidator;

    @Operation(summary = "Submit a new monitoring event")
    @PostMapping(
        path = "/api/events",
        consumes = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> submitEvent(@RequestBody final EventRequest event) {
        eventValidator.validateAndThrow(event);
        eventPublisher.publish(event);
        return ResponseEntity.status(ACCEPTED).build();
    }
}
