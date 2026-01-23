package io.github.eventify.api.watchlist.controller;

import io.github.eventify.api.watchlist.model.Watchlist;
import io.github.eventify.api.watchlist.model.WatchlistChannel;
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

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.*;
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

    @PostMapping(
        path = USER_WATCHLISTS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(CREATED)
    @Operation(
        summary = "Create watchlist",
        description = "Creates a new personal watchlist for the authenticated user"
    )
    public ResponseEntity<WatchlistDetailsResponse> createWatchlist(@RequestBody final CreateWatchlistRequest request) {
        watchlistValidator.validateAndThrow(request);
        final Watchlist watchlist = watchlistService.createWatchlist(request);
        final List<WatchlistChannel> channels = watchlistService.getWatchlistChannels(watchlist.getId());
        return ResponseEntity.status(CREATED).body(watchlistMapper.toDetailsResponse(watchlist, channels));
    }

    @PostMapping(
        path = USER_WATCHLISTS_SEARCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @Transactional(readOnly = true)
    @Operation(
        summary = "Search watchlists",
        description = "Searches personal watchlists with pagination, filtering, and sorting"
    )
    public ResponseEntity<PageResource<WatchlistDetailsResponse>> searchWatchlists(
        @RequestBody final SortablePageInput input
    ) {
        final Page<Watchlist> page = watchlistService.searchWatchlists(input);
        return ResponseEntity.status(OK).body(watchlistMapper.toPageResource(page));
    }

    @GetMapping(
        path = USER_WATCHLIST_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @Operation(
        summary = "Get watchlist",
        description = "Gets a personal watchlist by ID"
    )
    public ResponseEntity<WatchlistDetailsResponse> getWatchlist(@PathVariable final Long id) {
        final Watchlist watchlist = watchlistService.getWatchlist(id);
        final List<WatchlistChannel> channels = watchlistService.getWatchlistChannels(id);
        return ResponseEntity.status(OK).body(watchlistMapper.toDetailsResponse(watchlist, channels));
    }

    @PutMapping(
        path = USER_WATCHLIST_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @Operation(
        summary = "Update watchlist",
        description = "Updates a personal watchlist's details"
    )
    public ResponseEntity<WatchlistDetailsResponse> updateWatchlist(
        @PathVariable final Long id,
        @RequestBody final UpdateWatchlistRequest request
    ) {
        watchlistValidator.validateAndThrow(request);
        final Watchlist watchlist = watchlistService.updateWatchlist(id, request);
        final List<WatchlistChannel> channels = watchlistService.getWatchlistChannels(id);
        return ResponseEntity.status(OK).body(watchlistMapper.toDetailsResponse(watchlist, channels));
    }

    @DeleteMapping(
        path = USER_WATCHLIST_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @Operation(
        summary = "Delete watchlist",
        description = "Deletes a personal watchlist"
    )
    public ResponseEntity<Void> deleteWatchlist(@PathVariable final Long id) {
        watchlistService.deleteWatchlist(id);
        return ResponseEntity.status(OK).build();
    }
}
