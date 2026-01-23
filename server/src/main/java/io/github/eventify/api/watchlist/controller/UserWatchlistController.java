package io.github.eventify.api.watchlist.controller;

import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.mapper.WatchlistMapper;
import io.github.eventify.api.watchlist.model.request.CreateWatchlistRequest;
import io.github.eventify.api.watchlist.model.request.UpdateWatchlistRequest;
import io.github.eventify.api.watchlist.model.response.WatchlistDetailsResponse;
import io.github.eventify.api.watchlist.model.validator.WatchlistValidator;
import io.github.eventify.api.watchlist.service.WatchlistService;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for user watchlist management.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "User Watchlists",
    description = "Manage personal watchlists for event timeline monitoring"
)
public class UserWatchlistController {

    private final WatchlistService watchlistService;

    private final WatchlistMapper watchlistMapper;

    private final WatchlistValidator watchlistValidator;

    @ResponseStatus(CREATED)
    @Operation(
        summary = "Create watchlist",
        description = "Creates a new personal watchlist for the authenticated user"
    )
    @PostMapping(
        path = USER_WATCHLISTS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WatchlistDetailsResponse> createWatchlist(@RequestBody final CreateWatchlistRequest request) {
        watchlistValidator.validateAndThrow(request);
        final Watchlist watchlist = watchlistService.createWatchlist(watchlistMapper.toWatchlist(request));
        return ResponseEntity.status(CREATED).body(watchlistMapper.toResourceObject(watchlist));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Search watchlists",
        description = "Searches personal watchlists with pagination, filtering, and sorting"
    )
    @PostMapping(
        path = USER_WATCHLISTS_SEARCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageResource<WatchlistDetailsResponse>> searchWatchlists(@RequestBody final SortablePageInput input) {
        final Page<Watchlist> page = watchlistService.searchWatchlists(input);
        return ResponseEntity.status(OK).body(watchlistMapper.toPageResource(page));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Get watchlist",
        description = "Gets a personal watchlist by ID"
    )
    @GetMapping(
        path = USER_WATCHLIST_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WatchlistDetailsResponse> getWatchlist(@PathVariable final Long id) {
        final Watchlist watchlist = watchlistService.getWatchlist(id);
        return ResponseEntity.status(OK).body(watchlistMapper.toResourceObject(watchlist));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Update watchlist",
        description = "Updates a personal watchlist's details"
    )
    @PutMapping(
        path = USER_WATCHLIST_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WatchlistDetailsResponse> updateWatchlist(@PathVariable final Long id,
        @RequestBody final UpdateWatchlistRequest request) {
        watchlistValidator.validateAndThrow(request);
        final Watchlist updated = watchlistMapper.toWatchlist(request);
        final Watchlist watchlist = watchlistService.updateWatchlist(id, updated);
        return ResponseEntity.status(OK).body(watchlistMapper.toResourceObject(watchlist));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Delete watchlist",
        description = "Deletes a personal watchlist"
    )
    @DeleteMapping(
        path = USER_WATCHLIST_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Void> deleteWatchlist(@PathVariable final Long id) {
        watchlistService.deleteWatchlist(id);
        return ResponseEntity.status(OK).build();
    }
}
