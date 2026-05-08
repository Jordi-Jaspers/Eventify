package io.github.eventify.api.event.controller;

import io.github.eventify.api.event.model.Event;
import io.github.eventify.api.event.model.mapper.EventMapper;
import io.github.eventify.api.event.model.response.EventSearchResponse;
import io.github.eventify.api.event.service.EventService;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.USER_EVENTS_SEARCH_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for user event search operations.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "User Events",
    description = "User event search endpoints"
)
public class UserEventController {

    private final EventService eventService;

    private final EventMapper eventMapper;

    /**
     * Searches events for user's personal channels.
     *
     * @param input     the search input with pagination and filters
     * @param principal the authenticated user
     * @return page of event search results
     */
    @PostMapping(
        path = USER_EVENTS_SEARCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @Transactional(readOnly = true)
    @PreAuthorize("@eventSecurity.canSearchUserEvents(#input, #principal)")
    @Operation(
        summary = "Search user events",
        description = "Searches events from user's personal channels with pagination, filtering, and sorting"
    )
    public ResponseEntity<PageResource<EventSearchResponse>> searchEvents(
        @RequestBody final SortablePageInput input,
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        final Page<Event> page = eventService.searchUserEvents(input);
        return ResponseEntity.status(OK).body(eventMapper.toPageResource(page));
    }
}
