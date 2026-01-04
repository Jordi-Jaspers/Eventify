package io.github.eventify.api.admin.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Request for assigning an owner to an organization.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class AssignOwnerRequest {

    private String email;

    private Long userId;
}
