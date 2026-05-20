package io.github.eventify.api.notification.model.request;

import io.github.eventify.api.notification.model.NotificationAudienceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Request DTO for specifying a notification audience.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class AudienceRequest {

    private NotificationAudienceType type;

    private Long targetId;

    private String role;
}
