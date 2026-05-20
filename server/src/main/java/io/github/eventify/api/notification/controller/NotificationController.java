package io.github.eventify.api.notification.controller;

import io.github.eventify.api.notification.model.Notification;
import io.github.eventify.api.notification.model.mapper.NotificationMapper;
import io.github.eventify.api.notification.model.response.MarkAllReadResponse;
import io.github.eventify.api.notification.model.response.NotificationResponse;
import io.github.eventify.api.notification.model.response.UnreadCountResponse;
import io.github.eventify.api.notification.service.NotificationService;
import io.github.eventify.api.user.model.User;
import io.github.jframe.datasource.search.model.input.SortablePageInput;
import io.github.jframe.datasource.search.model.resource.PageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static io.github.eventify.api.Paths.NOTIFICATIONS_READ_ALL_PATH;
import static io.github.eventify.api.Paths.NOTIFICATIONS_SEARCH_PATH;
import static io.github.eventify.api.Paths.NOTIFICATIONS_UNREAD_COUNT_PATH;
import static io.github.eventify.api.Paths.NOTIFICATION_READ_PATH;
import static io.github.eventify.common.security.SecurityUtil.getLoggedInUser;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for notification management.
 */
@RestController
@RequiredArgsConstructor
@Tag(
    name = "Notification",
    description = "Notification management endpoints"
)
public class NotificationController {

    private final NotificationService notificationService;

    private final NotificationMapper notificationMapper;

    @ResponseStatus(OK)
    @Operation(
        summary = "Search notifications",
        description = "Returns paginated notifications for the authenticated user"
    )
    @PostMapping(
        path = NOTIFICATIONS_SEARCH_PATH,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<PageResource<NotificationResponse>> searchNotifications(
        @RequestBody final SortablePageInput input) {
        final User user = getLoggedInUser();
        final Page<Notification> page = notificationService.searchNotifications(user.getId(), input);
        return ResponseEntity.status(OK).body(notificationMapper.toPageResource(page));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Get unread count",
        description = "Returns the count of unread notifications for the authenticated user"
    )
    @GetMapping(
        path = NOTIFICATIONS_UNREAD_COUNT_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UnreadCountResponse> getUnreadCount() {
        final User user = getLoggedInUser();
        final long count = notificationService.getUnreadCount(user.getId());
        final UnreadCountResponse response = new UnreadCountResponse().setCount(count);
        return ResponseEntity.status(OK).body(response);
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(
        summary = "Mark notification as read",
        description = "Marks a single notification as read for the authenticated user"
    )
    @PostMapping(path = NOTIFICATION_READ_PATH)
    public ResponseEntity<Void> markAsRead(@PathVariable final Long id) {
        final User user = getLoggedInUser();
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Mark all notifications as read",
        description = "Marks all notifications as read for the authenticated user"
    )
    @PostMapping(
        path = NOTIFICATIONS_READ_ALL_PATH,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MarkAllReadResponse> markAllAsRead() {
        final User user = getLoggedInUser();
        final int markedCount = notificationService.markAllAsRead(user.getId());
        final MarkAllReadResponse response = new MarkAllReadResponse().setMarkedCount(markedCount);
        return ResponseEntity.status(OK).body(response);
    }
}
