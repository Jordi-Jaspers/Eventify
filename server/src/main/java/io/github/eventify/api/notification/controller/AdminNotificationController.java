package io.github.eventify.api.notification.controller;

import io.github.eventify.api.notification.model.NotificationBroadcast;
import io.github.eventify.api.notification.model.mapper.BroadcastMapper;
import io.github.eventify.api.notification.model.request.AudienceRequest;
import io.github.eventify.api.notification.model.request.CreateBroadcastRequest;
import io.github.eventify.api.notification.model.response.BroadcastResponse;
import io.github.eventify.api.notification.model.response.PreviewResponse;
import io.github.eventify.api.notification.model.validator.BroadcastValidator;
import io.github.eventify.api.notification.service.NotificationBroadcastService;
import io.github.eventify.api.user.model.User;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.ADMIN_BROADCASTS_PATH;
import static io.github.eventify.api.Paths.ADMIN_BROADCASTS_PREVIEW_PATH;
import static io.github.eventify.api.Paths.ADMIN_BROADCASTS_SEARCH_PATH;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for admin notification broadcast management.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Admin Notification",
    description = "Admin notification broadcast endpoints"
)
public class AdminNotificationController {

    private final NotificationBroadcastService notificationBroadcastService;

    private final BroadcastMapper broadcastMapper;

    private final BroadcastValidator broadcastValidator;

    @ResponseStatus(CREATED)
    @Operation(
        summary = "Send broadcast",
        description = "Sends a notification broadcast to the specified audience"
    )
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @PostMapping(
        path = ADMIN_BROADCASTS_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BroadcastResponse> sendBroadcast(
        @RequestBody final CreateBroadcastRequest request) {
        broadcastValidator.validateAndThrow(request);
        final User sender = getLoggedInUser();
        final NotificationBroadcast broadcast = notificationBroadcastService.sendBroadcast(sender, request);
        return ResponseEntity.status(CREATED).body(broadcastMapper.toResourceObject(broadcast));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "List broadcasts",
        description = "Returns paginated broadcasts ordered by createdAt DESC"
    )
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Transactional(readOnly = true)
    @PostMapping(
        path = ADMIN_BROADCASTS_SEARCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageResource<BroadcastResponse>> listBroadcasts(
        @RequestBody final SortablePageInput input) {
        final Page<NotificationBroadcast> page = notificationBroadcastService.searchBroadcasts(input);
        return ResponseEntity.status(OK).body(broadcastMapper.toPageResource(page));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Preview broadcast recipients",
        description = "Returns the recipient count for the given audience without creating a broadcast"
    )
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @PostMapping(
        path = ADMIN_BROADCASTS_PREVIEW_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PreviewResponse> previewBroadcast(
        @RequestBody final AudienceRequest audienceRequest) {
        broadcastValidator.validateAudience(audienceRequest);
        final int count = notificationBroadcastService.previewRecipientCount(audienceRequest);
        final PreviewResponse response = new PreviewResponse().setRecipientCount(count);
        return ResponseEntity.status(OK).body(response);
    }
}
