package io.github.eventify.api.channel.controller;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.mapper.ChannelMapper;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.request.UpdateChannelRequest;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.channel.model.validator.ChannelValidator;
import io.github.eventify.api.channel.service.OrganizationChannelService;
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
 * Controller for organization channel management.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Organization Channels",
    description = "Manage channels at organization level for event streaming"
)
public class OrganizationChannelController {

    private final OrganizationChannelService organizationChannelService;

    private final ChannelMapper channelMapper;

    private final ChannelValidator channelValidator;

    @PostMapping(
        path = ORGANIZATION_CHANNELS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(CREATED)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Create organization channel",
        description = "Creates a new organization channel. Requires OWNER or ADMIN role."
    )
    public ResponseEntity<ChannelDetailsResponse> createOrganizationChannel(
        @PathVariable final Long orgId,
        @RequestBody final CreateChannelRequest request
    ) {
        channelValidator.validateAndThrow(request);
        final Channel channel = organizationChannelService.createOrganizationChannel(orgId, request);
        return ResponseEntity.status(CREATED).body(channelMapper.toResourceObject(channel));
    }

    @PostMapping(
        path = ORGANIZATION_CHANNELS_SEARCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isMember(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Transactional(readOnly = true)
    @Operation(
        summary = "Search organization channels",
        description = "Searches organization channels with pagination, filtering, and sorting. Any member can view."
    )
    public ResponseEntity<PageResource<ChannelDetailsResponse>> searchOrganizationChannels(
        @PathVariable final Long orgId,
        @RequestBody final SortablePageInput input
    ) {
        final Page<Channel> page = organizationChannelService.searchOrganizationChannels(orgId, input);
        return ResponseEntity.status(OK).body(channelMapper.toPageResource(page));
    }

    @GetMapping(
        path = ORGANIZATION_CHANNEL_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isMember(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Get organization channel",
        description = "Gets an organization channel by ID. Any member can view."
    )
    public ResponseEntity<ChannelDetailsResponse> getOrganizationChannel(
        @PathVariable final Long orgId,
        @PathVariable final Long id
    ) {
        final Channel channel = organizationChannelService.getOrganizationChannel(orgId, id);
        return ResponseEntity.status(OK).body(channelMapper.toResourceObject(channel));
    }

    @PutMapping(
        path = ORGANIZATION_CHANNEL_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Update organization channel",
        description = "Updates an organization channel's details. Requires OWNER or ADMIN role."
    )
    public ResponseEntity<ChannelDetailsResponse> updateOrganizationChannel(
        @PathVariable final Long orgId,
        @PathVariable final Long id,
        @RequestBody final UpdateChannelRequest request
    ) {
        channelValidator.validateAndThrow(request);
        final Channel channel = organizationChannelService.updateOrganizationChannel(orgId, id, request);
        return ResponseEntity.status(OK).body(channelMapper.toResourceObject(channel));
    }

    @PostMapping(
        path = ORGANIZATION_CHANNEL_PAUSE_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Pause organization channel",
        description = "Pauses an organization channel (idempotent). Requires OWNER or ADMIN role."
    )
    public ResponseEntity<ChannelDetailsResponse> pauseOrganizationChannel(
        @PathVariable final Long orgId,
        @PathVariable final Long id
    ) {
        final Channel channel = organizationChannelService.pauseOrganizationChannel(orgId, id);
        return ResponseEntity.status(OK).body(channelMapper.toResourceObject(channel));
    }

    @PostMapping(
        path = ORGANIZATION_CHANNEL_RESUME_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Resume organization channel",
        description = "Resumes a paused organization channel (idempotent). Requires OWNER or ADMIN role."
    )
    public ResponseEntity<ChannelDetailsResponse> resumeOrganizationChannel(
        @PathVariable final Long orgId,
        @PathVariable final Long id
    ) {
        final Channel channel = organizationChannelService.resumeOrganizationChannel(orgId, id);
        return ResponseEntity.status(OK).body(channelMapper.toResourceObject(channel));
    }

    @DeleteMapping(
        path = ORGANIZATION_CHANNEL_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(OK)
    @PreAuthorize("@orgSecurity.isOwnerOrAdmin(#orgId, principal.user.id) or hasAuthority('MANAGE_ORGANIZATIONS')")
    @Operation(
        summary = "Delete organization channel",
        description = "Soft deletes an organization channel (sets status to PENDING_DELETION). Requires OWNER or ADMIN role."
    )
    public ResponseEntity<ChannelDetailsResponse> deleteOrganizationChannel(
        @PathVariable final Long orgId,
        @PathVariable final Long id
    ) {
        final Channel channel = organizationChannelService.deleteOrganizationChannel(orgId, id);
        return ResponseEntity.status(OK).body(channelMapper.toResourceObject(channel));
    }
}
