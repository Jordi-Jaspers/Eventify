package io.github.eventify.api.channel.controller;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.mapper.ChannelMapper;
import io.github.eventify.api.channel.model.request.ChannelBatchRequest;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.request.UpdateChannelRequest;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.channel.model.validator.ChannelValidator;
import io.github.eventify.api.channel.service.ChannelService;
import io.github.eventify.api.monitor.model.request.DurationDetailsRequest;
import io.github.eventify.api.monitor.model.response.DurationDetailsResponse;
import io.github.eventify.api.monitor.service.DurationService;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import static io.github.eventify.api.Paths.*;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for user channel management.
 */
@RestController
@RequiredArgsConstructor
@SuppressWarnings("ClassFanOutComplexity")
@Tag(
    name = "User Channels",
    description = "Manage personal channels for event streaming"
)
public class UserChannelController {

    private final ChannelService channelService;

    private final ChannelMapper channelMapper;

    private final ChannelValidator channelValidator;

    private final DurationService durationService;

    @PostMapping(
        path = USER_CHANNELS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(CREATED)
    @Operation(
        summary = "Create channel",
        description = "Creates a new personal channel for the authenticated user"
    )
    public ResponseEntity<ChannelDetailsResponse> createChannel(@RequestBody final CreateChannelRequest request) {
        channelValidator.validateAndThrow(request);
        final Channel channel = channelService.createUserChannel(request);
        return ResponseEntity.status(CREATED).body(channelMapper.toResourceObject(channel));
    }

    @PostMapping(
        path = USER_CHANNELS_SEARCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @Transactional(readOnly = true)
    @Operation(
        summary = "Search channels",
        description = "Searches personal channels with pagination, filtering, and sorting"
    )
    public ResponseEntity<PageResource<ChannelDetailsResponse>> searchChannels(@RequestBody final SortablePageInput input) {
        final Page<Channel> page = channelService.searchUserChannels(input);
        return ResponseEntity.status(OK).body(channelMapper.toPageResource(page));
    }

    @GetMapping(
        path = USER_CHANNEL_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@channelSecurity.canAccessChannelAsUser(#id, principal) or hasAuthority('MANAGE_USERS')")
    @Operation(
        summary = "Get channel",
        description = "Gets a personal channel by ID"
    )
    public ResponseEntity<ChannelDetailsResponse> getChannel(@PathVariable final Long id) {
        final Channel channel = channelService.getChannelWithAdminFallback(id);
        return ResponseEntity.status(OK).body(channelMapper.toResourceObject(channel));
    }

    @PutMapping(
        path = USER_CHANNEL_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@channelSecurity.canAccessChannelAsUser(#id, principal) or hasAuthority('MANAGE_USERS')")
    @Operation(
        summary = "Update channel",
        description = "Updates a personal channel's details"
    )
    public ResponseEntity<ChannelDetailsResponse> updateChannel(
        @PathVariable final Long id,
        @RequestBody final UpdateChannelRequest request
    ) {
        channelValidator.validateAndThrow(request);
        final Channel channel = channelService.updateUserChannel(id, request);
        return ResponseEntity.status(OK).body(channelMapper.toResourceObject(channel));
    }

    @PostMapping(
        path = USER_CHANNELS_PAUSE_PATH,
        consumes = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("@channelSecurity.canAccessChannelsAsUser(#request.channelIds, principal) or hasAuthority('MANAGE_USERS')")
    @Operation(
        summary = "Batch pause channels",
        description = "Pauses multiple personal channels in a single request (idempotent)"
    )
    public ResponseEntity<Void> batchPauseChannels(@RequestBody final ChannelBatchRequest request) {
        channelValidator.validateAndThrow(request);
        channelService.batchPauseUserChannels(request.getChannelIds());
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PostMapping(
        path = USER_CHANNELS_RESUME_PATH,
        consumes = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("@channelSecurity.canAccessChannelsAsUser(#request.channelIds, principal) or hasAuthority('MANAGE_USERS')")
    @Operation(
        summary = "Batch resume channels",
        description = "Resumes multiple personal channels in a single request (idempotent)"
    )
    public ResponseEntity<Void> batchResumeChannels(@RequestBody final ChannelBatchRequest request) {
        channelValidator.validateAndThrow(request);
        channelService.batchResumeUserChannels(request.getChannelIds());
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @DeleteMapping(
        path = USER_CHANNELS_PATH,
        consumes = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(NO_CONTENT)
    @PreAuthorize("@channelSecurity.canAccessChannelsAsUser(#request.channelIds, principal) or hasAuthority('MANAGE_USERS')")
    @Operation(
        summary = "Batch delete channels",
        description = "Soft deletes multiple personal channels in a single request (sets status to PENDING_DELETION)"
    )
    public ResponseEntity<Void> batchDeleteChannels(@RequestBody final ChannelBatchRequest request) {
        channelValidator.validateAndThrow(request);
        channelService.batchDeleteUserChannels(request.getChannelIds());
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @PostMapping(
        path = USER_CHANNEL_DURATIONS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@channelSecurity.canAccessChannelAsUser(#id, principal)")
    @Operation(
        summary = "Get channel duration details",
        description = "Fetches duration details around a specific timestamp for a user-owned channel"
    )
    public ResponseEntity<DurationDetailsResponse> getChannelDurations(
        @PathVariable final Long id,
        @RequestBody final DurationDetailsRequest request
    ) {
        final DurationDetailsResponse response = durationService.getDurations(
            id,
            request.getTimestamp(),
            request.getDirection()
        );
        return ResponseEntity.status(OK).body(response);
    }
}
