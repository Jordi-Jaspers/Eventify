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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.ORGANIZATION_EVENTS_SEARCH_PATH;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for organization event search operations.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Organization Events",
    description = "Organization event search endpoints"
)
public class OrganizationEventController {

    private final EventService eventService;

    private final EventMapper eventMapper;

    /**
     * Searches events for organization channels.
     *
     * @param orgId     the organization ID
     * @param input     the search input with pagination and filters
     * @param principal the authenticated user
     * @return page of event search results
     */
    @PostMapping(
        path = ORGANIZATION_EVENTS_SEARCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @Transactional(readOnly = true)
    @PreAuthorize("@eventSecurity.canSearchOrganizationEvents(#orgId, #input, #principal)")
    @Operation(
        summary = "Search organization events",
        description = "Searches events from organization channels with pagination, filtering, and sorting"
    )
    public ResponseEntity<PageResource<EventSearchResponse>> searchEvents(
        @PathVariable("orgId") final Long orgId,
        @RequestBody final SortablePageInput input,
        @AuthenticationPrincipal final UserTokenPrincipal principal) {
        final Page<Event> page = eventService.searchOrganizationEvents(orgId, input);
        return ResponseEntity.status(OK).body(eventMapper.toPageResource(page));
    }
}
