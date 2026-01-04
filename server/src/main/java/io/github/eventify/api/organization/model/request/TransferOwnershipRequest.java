package io.github.eventify.api.organization.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(
        description = "User ID of the current organization owner",
        example = "12345",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long currentOwnerUserId;

    @Schema(
        description = "User ID of the new organization owner",
        example = "67890",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long newOwnerUserId;
}
