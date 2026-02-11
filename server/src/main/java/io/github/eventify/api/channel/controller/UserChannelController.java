package io.github.eventify.api.channel.controller;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.mapper.ChannelMapper;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.request.UpdateChannelRequest;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.channel.model.validator.ChannelValidator;
import io.github.eventify.api.channel.service.ChannelService;
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
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for user channel management.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "User Channels",
    description = "Manage personal channels for event streaming"
)
public class UserChannelController {

    private final ChannelService channelService;

    private final ChannelMapper channelMapper;

    private final ChannelValidator channelValidator;

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
        path = USER_CHANNEL_PAUSE_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@channelSecurity.canAccessChannelAsUser(#id, principal) or hasAuthority('MANAGE_USERS')")
    @Operation(
        summary = "Pause channel",
        description = "Pauses a personal channel (idempotent)"
    )
    public ResponseEntity<ChannelDetailsResponse> pauseChannel(@PathVariable final Long id) {
        final Channel channel = channelService.pauseUserChannel(id);
        return ResponseEntity.status(OK).body(channelMapper.toResourceObject(channel));
    }

    @PostMapping(
        path = USER_CHANNEL_RESUME_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@channelSecurity.canAccessChannelAsUser(#id, principal) or hasAuthority('MANAGE_USERS')")
    @Operation(
        summary = "Resume channel",
        description = "Resumes a paused channel (idempotent)"
    )
    public ResponseEntity<ChannelDetailsResponse> resumeChannel(@PathVariable final Long id) {
        final Channel channel = channelService.resumeUserChannel(id);
        return ResponseEntity.status(OK).body(channelMapper.toResourceObject(channel));
    }

    @DeleteMapping(
        path = USER_CHANNEL_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@channelSecurity.canAccessChannelAsUser(#id, principal) or hasAuthority('MANAGE_USERS')")
    @Operation(
        summary = "Delete channel",
        description = "Soft deletes a personal channel (sets status to PENDING_DELETION)"
    )
    public ResponseEntity<ChannelDetailsResponse> deleteChannel(@PathVariable final Long id) {
        final Channel channel = channelService.deleteUserChannel(id);
        return ResponseEntity.status(OK).body(channelMapper.toResourceObject(channel));
    }
}
