package io.github.eventify.api.watchlist.controller;

import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.mapper.WatchlistMapper;
import io.github.eventify.api.watchlist.model.request.CreateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.UpdateWatchlistRequest;
import io.github.eventify.api.watchlist.model.response.WatchlistDetailsResponse;
import io.github.eventify.api.watchlist.model.validator.WatchlistValidator;
import io.github.eventify.api.watchlist.service.OrganizationWatchlistService;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for organization watchlist management.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Organization Watchlists",
    description = "Manage watchlists at organization level for event timeline monitoring"
)
public class OrganizationWatchlistController {

    private final OrganizationWatchlistService organizationWatchlistService;

    private final WatchlistMapper watchlistMapper;

    private final WatchlistValidator watchlistValidator;

    @ResponseStatus(CREATED)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Create organization watchlist",
        description = "Creates a new organization watchlist. Requires OWNER or ADMIN role."
    )
    @PostMapping(
        path = ORGANIZATION_WATCHLISTS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WatchlistDetailsResponse> createOrganizationWatchlist(
        @PathVariable final Long orgId,
        @RequestBody final CreateWatchlistRequest request
    ) {
        watchlistValidator.validateAndThrow(request);
        final Watchlist watchlist = organizationWatchlistService.createWatchlist(orgId, watchlistMapper.toWatchlist(request));
        return ResponseEntity.status(CREATED).body(watchlistMapper.toResourceObject(watchlist));
    }

    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isMember(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Search organization watchlists",
        description = "Searches organization watchlists with pagination, filtering, and sorting. Any member can view."
    )
    @PostMapping(
        path = ORGANIZATION_WATCHLISTS_SEARCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageResource<WatchlistDetailsResponse>> searchOrganizationWatchlists(
        @PathVariable final Long orgId,
        @RequestBody final SortablePageInput input
    ) {
        final Page<Watchlist> page = organizationWatchlistService.searchWatchlists(orgId, input);
        return ResponseEntity.status(OK).body(watchlistMapper.toPageResource(page));
    }

    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isMember(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Get organization watchlist",
        description = "Gets an organization watchlist by ID. Any member can view."
    )
    @GetMapping(
        path = ORGANIZATION_WATCHLIST_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WatchlistDetailsResponse> getOrganizationWatchlist(
        @PathVariable final Long orgId,
        @PathVariable final Long id
    ) {
        final Watchlist watchlist = organizationWatchlistService.getWatchlist(orgId, id);
        return ResponseEntity.status(OK).body(watchlistMapper.toResourceObject(watchlist));
    }

    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Update organization watchlist",
        description = "Updates an organization watchlist's details. Requires OWNER or ADMIN role."
    )
    @PutMapping(
        path = ORGANIZATION_WATCHLIST_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WatchlistDetailsResponse> updateOrganizationWatchlist(
        @PathVariable final Long orgId,
        @PathVariable final Long id,
        @RequestBody final UpdateWatchlistRequest request
    ) {
        watchlistValidator.validateAndThrow(request);
        final Watchlist updated = watchlistMapper.toWatchlist(request);
        final Watchlist watchlist = organizationWatchlistService.updateWatchlist(orgId, id, updated);
        return ResponseEntity.status(OK).body(watchlistMapper.toResourceObject(watchlist));
    }

    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Delete organization watchlist",
        description = "Deletes an organization watchlist. Requires OWNER or ADMIN role."
    )
    @DeleteMapping(
        path = ORGANIZATION_WATCHLIST_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> deleteOrganizationWatchlist(
        @PathVariable final Long orgId,
        @PathVariable final Long id
    ) {
        organizationWatchlistService.deleteWatchlist(orgId, id);
        return ResponseEntity.status(OK).build();
    }
}
