package io.github.eventify.api.event.controller;

import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.mapper.EventMapper;
import io.github.eventify.api.event.model.request.CreateEventRequest;
import io.github.eventify.api.event.model.response.EventCreatedResponse;
import io.github.eventify.api.event.model.validator.CreateEventValidator;
import io.github.eventify.api.event.service.EventIngestionService;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
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

import static io.github.eventify.api.Paths.EVENTS_PATH;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for event ingestion API.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Event Ingestion",
    description = "Real-time event ingestion API"
)
public class EventIngestionController {

    private final EventIngestionService eventIngestionService;

    private final CreateEventValidator createEventValidator;

    private final EventMapper eventMapper;

    @ResponseStatus(CREATED)
    @Operation(
        summary = "Ingest event",
        description = "Creates a new event in the specified channel"
    )
    @PostMapping(
        path = EVENTS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@channelSecurity.canAccess(#request.channelId, principal)")
    public ResponseEntity<EventCreatedResponse> ingestEvent(@RequestBody final CreateEventRequest request,
        @AuthenticationPrincipal final ApiKeyPrincipal principal) {
        createEventValidator.validateAndThrow(request);
        final Event event = eventIngestionService.ingestEvent(request);
        final EventCreatedResponse response = eventMapper.toCreatedResponse(event);
        return ResponseEntity.status(CREATED).body(response);
    }
}
