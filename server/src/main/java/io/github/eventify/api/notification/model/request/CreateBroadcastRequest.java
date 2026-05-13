package io.github.eventify.api.notification.model.request;

import io.github.eventify.api.notification.model.NotificationCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Request DTO for creating a notification broadcast.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class CreateBroadcastRequest {

    private NotificationCategory category;

    private String title;

    private String message;

    private String actionUrl;

    private String actionLabel;

    private AudienceRequest audience;
}
