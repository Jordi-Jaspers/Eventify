package io.github.eventify.api.notification.model.request;

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

    private String type;

    private Long targetId;

    private String role;
}
