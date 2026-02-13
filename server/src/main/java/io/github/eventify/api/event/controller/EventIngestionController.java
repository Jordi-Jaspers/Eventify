package io.github.eventify.api.event.controller;

import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.mapper.EventMapper;
import io.github.eventify.api.event.model.request.BatchEventRequest;
import io.github.eventify.api.event.model.request.CreateEventRequest;
import io.github.eventify.api.event.model.response.EventCreatedResponse;
import io.github.eventify.api.event.model.validator.EventValidator;
import io.github.eventify.api.event.service.EventIngestionService;
import io.github.eventify.api.quota.service.UserQuotaService;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.EXTERNAL_EVENTS_BATCH_PATH;
import static io.github.eventify.api.Paths.EXTERNAL_EVENTS_PATH;
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

    private final EventValidator eventValidator;

    private final EventMapper eventMapper;

    private final UserQuotaService userQuotaService;

    @ResponseStatus(CREATED)
    @Operation(
        summary = "Ingest event",
        description = "Creates a new event in the specified channel"
    )
    @PostMapping(
        path = EXTERNAL_EVENTS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@channelSecurity.canAccess(#request.channelId, principal)")
    public ResponseEntity<EventCreatedResponse> ingestEvent(@RequestBody final CreateEventRequest request,
        @AuthenticationPrincipal final ApiKeyPrincipal principal) {
        eventValidator.validateAndThrow(request);
        // Only enforce quota for personal API keys (organization API keys have unlimited usage)
        if (principal.isUserKey()) {
            userQuotaService.checkAndIncrementOrThrow(principal.getUserId(), 1);
        }
        final Event event = eventIngestionService.ingestEvent(request);
        return ResponseEntity.status(CREATED).body(eventMapper.toCreatedResponse(event));
    }

    @ResponseStatus(CREATED)
    @Operation(
        summary = "Ingest batch of events",
        description = "Creates multiple events in a single request with client-provided timestamps. All-or-nothing semantics."
    )
    @PostMapping(
        path = EXTERNAL_EVENTS_BATCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@channelSecurity.canAccessBatch(#request, principal)")
    public ResponseEntity<List<EventCreatedResponse>> ingestBatch(@RequestBody final BatchEventRequest request,
        @AuthenticationPrincipal final ApiKeyPrincipal principal) {
        eventValidator.validateAndThrow(request);
        // Only enforce quota for personal API keys (organization API keys have unlimited usage)
        if (principal.isUserKey()) {
            userQuotaService.checkAndIncrementOrThrow(principal.getUserId(), request.getEvents().size());
        }
        final List<Event> events = eventIngestionService.ingestBatch(request);
        return ResponseEntity.status(CREATED).body(eventMapper.toCreatedResponseList(events));
    }
}
