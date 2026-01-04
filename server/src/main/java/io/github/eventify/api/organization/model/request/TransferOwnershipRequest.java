package io.github.eventify.api.organization.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Request for transferring organization ownership.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class TransferOwnershipRequest {

    private Long currentOwnerUserId;
    private Long newOwnerUserId;
}
