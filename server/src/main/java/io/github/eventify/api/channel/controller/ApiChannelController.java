package io.github.eventify.api.channel.controller;

import io.github.eventify.api.channel.model.Channel;
import io.github.eventify.api.channel.model.mapper.ChannelMapper;
import io.github.eventify.api.channel.model.request.CreateChannelRequest;
import io.github.eventify.api.channel.model.response.ChannelDetailsResponse;
import io.github.eventify.api.channel.model.validator.ChannelValidator;
import io.github.eventify.api.channel.service.ApiChannelService;
import io.github.eventify.common.security.principal.ApiKeyPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.EXTERNAL_CHANNELS_PATH;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Controller for channel management via API key authentication.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "External Channels",
    description = "Channel management via API key"
)
public class ApiChannelController {

    private final ApiChannelService apiChannelService;

    private final ChannelMapper channelMapper;

    private final ChannelValidator channelValidator;

    @PostMapping(
        path = EXTERNAL_CHANNELS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    @ResponseStatus(CREATED)
    @Operation(
        summary = "Create channel",
        description = "Creates a channel via API key authentication with provided name and slug."
    )
    public ResponseEntity<ChannelDetailsResponse> createChannel(@RequestBody final CreateChannelRequest request,
        @AuthenticationPrincipal final ApiKeyPrincipal principal) {
        channelValidator.validateAndThrow(request);
        final Channel channel = apiChannelService.createChannel(request, principal);
        return ResponseEntity.status(CREATED).body(channelMapper.toResourceObject(channel));
    }
}
